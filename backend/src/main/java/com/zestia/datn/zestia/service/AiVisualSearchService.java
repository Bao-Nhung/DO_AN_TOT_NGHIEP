package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.AiVisualSearchLog;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.SanPham;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.AiVisualSearchLogRepository;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVisualSearchService {

    private static final int MAX_IMAGE_SIDE = 6_000;
    private static final long MAX_IMAGE_PIXELS = 25_000_000L;
    private static final Map<String, Rgb> NAMED_COLORS = Map.ofEntries(
            Map.entry("Đen", new Rgb(24, 24, 24)),
            Map.entry("Trắng", new Rgb(242, 242, 238)),
            Map.entry("Xám", new Rgb(135, 135, 135)),
            Map.entry("Đỏ", new Rgb(185, 48, 55)),
            Map.entry("Hồng", new Rgb(226, 143, 166)),
            Map.entry("Cam", new Rgb(220, 116, 42)),
            Map.entry("Vàng", new Rgb(211, 173, 42)),
            Map.entry("Xanh lá", new Rgb(54, 126, 82)),
            Map.entry("Xanh dương", new Rgb(55, 91, 154)),
            Map.entry("Tím", new Rgb(119, 73, 145)),
            Map.entry("Nâu", new Rgb(119, 78, 57)),
            Map.entry("Kem", new Rgb(218, 200, 169))
    );

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository variantRepository;
    private final HoaDonChiTietRepository orderDetailRepository;
    private final AnhRepository imageRepository;
    private final PromotionPricingService promotionPricingService;
    private final AiVisualSearchLogRepository searchLogRepository;

    @Transactional
    public Map<String, Object> searchByImage(MultipartFile file) {
        BufferedImage image = readValidatedImage(file);
        Rgb dominantColor = dominantColor(image);
        String colorName = nearestColorName(dominantColor);

        List<SanPham> products = sanPhamRepository.findByTrangThai((byte) 1);
        List<Integer> productIds = products.stream().map(SanPham::getId).filter(Objects::nonNull).toList();
        Map<Integer, List<SanPhamChiTiet>> variantsByProduct = productIds.isEmpty()
                ? Map.of()
                : variantRepository.findBySanPhamIdIn(productIds).stream()
                        .filter(this::sellableVariant)
                        .collect(java.util.stream.Collectors.groupingBy(v -> v.getSanPham().getId()));
        Map<Integer, String> productImages = loadProductImages(productIds);

        List<Map<String, Object>> matches = new ArrayList<>();
        for (SanPham product : products) {
            SanPhamChiTiet closestVariant = variantsByProduct.getOrDefault(product.getId(), List.of()).stream()
                    .min(Comparator.comparingDouble(variant -> colorDistance(dominantColor, variantColor(variant))))
                    .orElse(null);
            if (closestVariant == null) continue;

            double distance = colorDistance(dominantColor, variantColor(closestVariant));
            int similarity = (int) Math.round(Math.max(0, 100 - (distance / 441.67295593d * 100)));
            PromotionPricingService.PriceQuote quote = promotionPricingService.quote(closestVariant);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", product.getId());
            item.put("variantId", closestVariant.getId());
            item.put("maSanPham", product.getMaSanPham());
            item.put("tenSanPham", product.getTenSanPham());
            item.put("giaGoc", quote.basePrice());
            item.put("giaCuoi", quote.effectivePrice());
            item.put("anhChinh", firstNonBlank(closestVariant.getAnhUrl(), productImages.get(product.getId())));
            item.put("matchScore", similarity);
            item.put("mauGanNhat", closestVariant.getMauSac() != null ? closestVariant.getMauSac().getTenMauSac() : null);
            item.put("kichThuoc", closestVariant.getKichThuoc() != null ? closestVariant.getKichThuoc().getTenKichThuoc() : null);
            item.put("danhMuc", product.getLoaiSanPham() != null ? product.getLoaiSanPham().getTenLoaiSanPham() : "Thời trang");
            matches.add(item);
        }

        matches.sort(Comparator.comparingInt(item -> -((Number) item.get("matchScore")).intValue()));
        List<Map<String, Object>> limitedMatches = matches.stream().limit(12).toList();
        saveSearchLog(file, colorName, dominantColor, limitedMatches);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("detectedTags", List.of(
                "Màu nổi bật: " + colorName + " (" + dominantColor.hex() + ")",
                "Kết quả được xếp theo độ gần màu của biến thể còn hàng"
        ));
        result.put("detectedColor", colorName);
        result.put("detectedHex", dominantColor.hex());
        result.put("totalFound", matches.size());
        result.put("results", limitedMatches);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFrequentlyBoughtTogether(Integer productId) {
        SanPham mainProduct = sanPhamRepository.findById(productId)
                .filter(product -> Byte.valueOf((byte) 1).equals(product.getTrangThai()))
                .orElse(null);
        if (mainProduct == null) return emptyFrequentlyBoughtTogether();

        List<Integer> suggestedIds = orderDetailRepository
                .findFrequentlyBoughtProductIds(productId, PageRequest.of(0, 3)).stream()
                .map(row -> ((Number) row[0]).intValue())
                .toList();
        if (suggestedIds.isEmpty()) return emptyFrequentlyBoughtTogether();

        List<Integer> allIds = new ArrayList<>();
        allIds.add(productId);
        allIds.addAll(suggestedIds);
        Map<Integer, SanPham> productsById = new HashMap<>();
        sanPhamRepository.findAllById(allIds).forEach(product -> productsById.put(product.getId(), product));
        Map<Integer, List<SanPhamChiTiet>> variantsByProduct = variantRepository.findBySanPhamIdIn(allIds).stream()
                .filter(this::sellableVariant)
                .collect(java.util.stream.Collectors.groupingBy(v -> v.getSanPham().getId()));
        Map<Integer, String> productImages = loadProductImages(allIds);

        List<Map<String, Object>> items = new ArrayList<>();
        appendProductItem(items, productsById.get(productId), variantsByProduct, productImages);
        for (Integer suggestedId : suggestedIds) {
            appendProductItem(items, productsById.get(suggestedId), variantsByProduct, productImages);
        }
        if (items.size() < 2) return emptyFrequentlyBoughtTogether();

        BigDecimal totalPrice = items.stream()
                .map(item -> (BigDecimal) item.getOrDefault("price", BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOriginalPrice = items.stream()
                .map(item -> (BigDecimal) item.getOrDefault("originalPrice", item.getOrDefault("price", BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of(
                "items", items,
                "totalPrice", totalPrice,
                "totalOriginalPrice", totalOriginalPrice
        );
    }

    private void appendProductItem(List<Map<String, Object>> items,
                                   SanPham product,
                                   Map<Integer, List<SanPhamChiTiet>> variantsByProduct,
                                   Map<Integer, String> productImages) {
        if (product == null || !Byte.valueOf((byte) 1).equals(product.getTrangThai())) return;
        SanPhamChiTiet variant = variantsByProduct.getOrDefault(product.getId(), List.of()).stream()
                .min(Comparator.comparing(v -> promotionPricingService.quote(v).effectivePrice()))
                .orElse(null);
        if (variant == null) return;
        PromotionPricingService.PriceQuote quote = promotionPricingService.quote(variant);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", product.getId());
        item.put("variantId", variant.getId());
        item.put("name", product.getTenSanPham());
        item.put("price", quote.effectivePrice());
        item.put("originalPrice", quote.basePrice());
        item.put("promotionActive", quote.discounted());
        item.put("campaign", quote.campaignName());
        item.put("image", firstNonBlank(variant.getAnhUrl(), productImages.get(product.getId())));
        item.put("color", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
        item.put("size", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
        item.put("stock", variant.getSoLuong());
        items.add(item);
    }

    private Map<String, Object> emptyFrequentlyBoughtTogether() {
        return Map.of(
                "items", List.of(),
                "totalPrice", BigDecimal.ZERO,
                "totalOriginalPrice", BigDecimal.ZERO
        );
    }

    private BufferedImage readValidatedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn một tệp hình ảnh.");
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            if (input == null) throw new IllegalArgumentException("Không đọc được tệp hình ảnh.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Định dạng ảnh không được hỗ trợ.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_SIDE || height > MAX_IMAGE_SIDE
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("Kích thước ảnh quá lớn. Vui lòng chọn ảnh tối đa 6000px mỗi chiều.");
                }
                BufferedImage image = reader.read(0);
                if (image == null) throw new IllegalArgumentException("Không đọc được nội dung hình ảnh.");
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Tệp hình ảnh bị lỗi hoặc không được hỗ trợ.", exception);
        }
    }

    private Rgb dominantColor(BufferedImage image) {
        int step = Math.max(1, Math.max(image.getWidth(), image.getHeight()) / 240);
        Map<Integer, ColorBucket> histogram = collectHistogram(image, step, true);
        if (histogram.isEmpty()) histogram = collectHistogram(image, step, false);
        return histogram.values().stream()
                .max(Comparator.comparingLong(ColorBucket::count))
                .map(ColorBucket::average)
                .orElse(new Rgb(128, 128, 128));
    }

    private Map<Integer, ColorBucket> collectHistogram(BufferedImage image, int step, boolean ignoreBackground) {
        Map<Integer, ColorBucket> histogram = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                if (alpha < 128) continue;
                if (ignoreBackground && red > 242 && green > 242 && blue > 242) continue;
                int key = (red / 24 << 16) | (green / 24 << 8) | (blue / 24);
                histogram.computeIfAbsent(key, ignored -> new ColorBucket()).add(red, green, blue);
            }
        }
        return histogram;
    }

    private boolean sellableVariant(SanPhamChiTiet variant) {
        return variant != null
                && variant.getSanPham() != null
                && Byte.valueOf((byte) 1).equals(variant.getSanPham().getTrangThai())
                && Byte.valueOf((byte) 1).equals(variant.getTrangThai())
                && variant.getSoLuong() != null
                && variant.getSoLuong() > 0
                && variant.getMauSac() != null;
    }

    private Rgb variantColor(SanPhamChiTiet variant) {
        String hex = variant.getMauSac() != null ? variant.getMauSac().getMaHex() : null;
        Rgb parsed = parseHex(hex);
        if (parsed != null) return parsed;
        String name = variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : "";
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return NAMED_COLORS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey().toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new Rgb(128, 128, 128));
    }

    private Rgb parseHex(String value) {
        if (value == null || !value.matches("^#[0-9a-fA-F]{6}$")) return null;
        return new Rgb(
                Integer.parseInt(value.substring(1, 3), 16),
                Integer.parseInt(value.substring(3, 5), 16),
                Integer.parseInt(value.substring(5, 7), 16)
        );
    }

    private double colorDistance(Rgb first, Rgb second) {
        int red = first.red() - second.red();
        int green = first.green() - second.green();
        int blue = first.blue() - second.blue();
        return Math.sqrt(red * red + green * green + blue * blue);
    }

    private String nearestColorName(Rgb color) {
        return NAMED_COLORS.entrySet().stream()
                .min(Comparator.comparingDouble(entry -> colorDistance(color, entry.getValue())))
                .map(Map.Entry::getKey)
                .orElse("Không xác định");
    }

    private Map<Integer, String> loadProductImages(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();
        Map<Integer, String> images = new HashMap<>();
        for (Anh image : imageRepository.findBySanPhamIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1)) {
            if (image.getSanPham() != null && image.getAnhUrl() != null && !image.getAnhUrl().isBlank()) {
                images.putIfAbsent(image.getSanPham().getId(), image.getAnhUrl());
            }
        }
        return images;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        return second != null && !second.isBlank() ? second : null;
    }

    private void saveSearchLog(MultipartFile file, String colorName, Rgb color,
                               List<Map<String, Object>> matches) {
        try {
            String ids = matches.stream()
                    .limit(5)
                    .map(item -> String.valueOf(item.get("id")))
                    .collect(java.util.stream.Collectors.joining(","));
            int topSimilarity = matches.isEmpty() ? 0 : ((Number) matches.get(0).get("matchScore")).intValue();
            searchLogRepository.save(AiVisualSearchLog.builder()
                    .imageFilename(file.getOriginalFilename())
                    .detectedCategory("Phân tích màu")
                    .detectedColor(colorName + " " + color.hex())
                    .matchedProductIds(ids)
                    .highestScore(topSimilarity)
                    .ngayTao(LocalDateTime.now())
                    .build());
        } catch (RuntimeException exception) {
            log.warn("Không thể lưu lịch sử tìm sản phẩm theo màu ảnh", exception);
        }
    }

    private record Rgb(int red, int green, int blue) {
        private String hex() {
            return String.format(Locale.ROOT, "#%02X%02X%02X", red, green, blue);
        }
    }

    private static final class ColorBucket {
        private long red;
        private long green;
        private long blue;
        private long count;

        private void add(int red, int green, int blue) {
            this.red += red;
            this.green += green;
            this.blue += blue;
            count++;
        }

        private long count() {
            return count;
        }

        private Rgb average() {
            return new Rgb((int) (red / count), (int) (green / count), (int) (blue / count));
        }
    }
}
