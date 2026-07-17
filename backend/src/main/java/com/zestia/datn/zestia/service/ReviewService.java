package com.zestia.datn.zestia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.DanhGia;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.Vay;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.DanhGiaRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.VayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final int MAX_IMAGES = 3;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final DanhGiaRepository reviewRepo;
    private final AnhRepository imageRepo;
    private final HoaDonChiTietRepository orderDetailRepo;
    private final HoaDonRepository orderRepo;
    private final VayRepository productRepo;
    private final CurrentCustomerService currentCustomerService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> reviews(Integer productId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        var result = reviewRepo.findByVayIdAndTrangThai(
                productId,
                (byte) 1,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "ngayTao"))
        );
        Map<Integer, String> productImages = productImages(result.getContent());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream()
                .map(review -> toMap(review, productImages))
                .toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("summary", summary(productId));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> storeReviews(int page, int size, Integer stars) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(24, Math.max(1, size));
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "ngayTao"));
        var result = stars != null && stars >= 1 && stars <= 5
                ? reviewRepo.findByTrangThaiAndSoSao((byte) 1, stars.byteValue(), pageable)
                : reviewRepo.findByTrangThai((byte) 1, pageable);
        Map<Integer, String> productImages = productImages(result.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream()
                .map(review -> toMap(review, productImages))
                .toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("summary", storeSummary());
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> eligibility(Integer productId, Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        List<Integer> deliveredIds = orderDetailRepo.findDeliveredOrderIdsForCustomerAndProduct(customer.getId(), productId);
        List<Integer> eligibleIds = deliveredIds.stream()
                .filter(orderId -> !reviewRepo.existsByKhachHangIdAndVayIdAndHoaDonId(customer.getId(), productId, orderId))
                .toList();
        List<Map<String, Object>> orders = orderRepo.findAllById(eligibleIds).stream()
                .sorted(Comparator.comparing(HoaDon::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(order -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", order.getId());
                    item.put("maHoaDon", order.getMaHoaDon());
                    item.put("ngayTao", order.getNgayTao());
                    return item;
                })
                .toList();
        return Map.of("canReview", !orders.isEmpty(), "orders", orders);
    }

    @Transactional
    public Map<String, Object> create(Integer productId, Integer orderId, int stars, String content,
                                      List<MultipartFile> images, Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        if (stars < 1 || stars > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Điểm đánh giá phải từ 1 đến 5 sao");
        }
        String cleanContent = content != null ? content.trim() : "";
        if (cleanContent.length() < 10 || cleanContent.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung đánh giá cần từ 10 đến 2000 ký tự");
        }

        Vay product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        HoaDon order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        List<Integer> eligibleOrderIds = orderDetailRepo.findDeliveredOrderIdsForCustomerAndProduct(customer.getId(), productId);
        if (!eligibleOrderIds.contains(order.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ khách đã nhận sản phẩm mới được đánh giá");
        }
        if (reviewRepo.existsByKhachHangIdAndVayIdAndHoaDonId(customer.getId(), productId, orderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm trong đơn này đã được đánh giá");
        }

        List<String> imageUrls = saveImages(productId, customer.getId(), images);
        try {
            DanhGia review = reviewRepo.save(DanhGia.builder()
                    .khachHang(customer)
                    .vay(product)
                    .hoaDon(order)
                    .soSao((byte) stars)
                    .noiDung(cleanContent)
                    .anhDanhGia(imageUrls.isEmpty() ? null : objectMapper.writeValueAsString(imageUrls))
                    .trangThai((byte) 1)
                    .ngayTao(LocalDateTime.now())
                    .build());
            return toMap(review, Map.of());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu ảnh đánh giá");
        }
    }

    public Map<String, Object> summary(Integer productId) {
        DanhGiaRepository.ReviewSummary raw = reviewRepo.summarizeProduct(productId);
        Number average = raw != null && raw.getAverage() != null ? raw.getAverage() : 0;
        Number count = raw != null && raw.getTotal() != null ? raw.getTotal() : 0;
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) distribution.put(star, 0L);
        for (Object[] row : reviewRepo.distributionForProduct(productId)) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return Map.of(
                "average", BigDecimal.valueOf(average.doubleValue()).setScale(1, RoundingMode.HALF_UP),
                "count", count.longValue(),
                "distribution", distribution
        );
    }

    public Map<String, Object> storeSummary() {
        DanhGiaRepository.ReviewSummary raw = reviewRepo.summarizeStore();
        Number average = raw != null && raw.getAverage() != null ? raw.getAverage() : 0;
        Number count = raw != null && raw.getTotal() != null ? raw.getTotal() : 0;
        Map<Integer, Long> distribution = emptyDistribution();
        for (Object[] row : reviewRepo.distributionForStore()) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return Map.of(
                "average", BigDecimal.valueOf(average.doubleValue()).setScale(1, RoundingMode.HALF_UP),
                "count", count.longValue(),
                "distribution", distribution
        );
    }

    private Map<String, Object> toMap(DanhGia review, Map<Integer, String> productImages) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("customerName", CustomerPrivacy.maskName(review.getKhachHang().getHoVaTen()));
        map.put("productId", review.getVay().getId());
        map.put("productName", review.getVay().getTenVay());
        map.put("productImage", productImages.get(review.getVay().getId()));
        map.put("stars", review.getSoSao());
        map.put("content", review.getNoiDung());
        map.put("images", parseImages(review.getAnhDanhGia()));
        map.put("verifiedPurchase", review.getHoaDon() != null);
        map.put("createdAt", review.getNgayTao());
        return map;
    }

    private Map<Integer, Long> emptyDistribution() {
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) distribution.put(star, 0L);
        return distribution;
    }

    private Map<Integer, String> productImages(List<DanhGia> reviews) {
        List<Integer> productIds = reviews.stream()
                .map(review -> review.getVay().getId())
                .distinct()
                .toList();
        if (productIds.isEmpty()) return Map.of();

        Map<Integer, String> images = new LinkedHashMap<>();
        for (Anh image : imageRepo.findByVayIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1)) {
            images.putIfAbsent(image.getVay().getId(), image.getAnhUrl());
        }
        return images;
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, new TypeReference<>() {});
            }
            return List.of(json);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> saveImages(Integer productId, Integer customerId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return List.of();
        if (images.size() > MAX_IMAGES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mỗi đánh giá được tải tối đa 3 ảnh");
        }
        Path directory = resolveReviewUploadDir();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo thư mục ảnh đánh giá");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;
            if (image.getSize() > MAX_IMAGE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mỗi ảnh đánh giá tối đa 5MB");
            }
            String contentType = Optional.ofNullable(image.getContentType()).orElse("").toLowerCase(Locale.ROOT);
            String extension = "image/png".equals(contentType) ? ".png" : "image/jpeg".equals(contentType) ? ".jpg" : null;
            if (extension == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh đánh giá chỉ hỗ trợ JPG hoặc PNG");
            }
            try {
                if (ImageIO.read(image.getInputStream()) == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tệp tải lên không phải ảnh hợp lệ");
                }
                String filename = "review_" + productId + "_" + customerId + "_" + UUID.randomUUID() + extension;
                Path storedFile = directory.resolve(filename);
                Files.copy(image.getInputStream(), storedFile, StandardCopyOption.REPLACE_EXISTING);
                registerRollbackDelete(storedFile);
                urls.add("/images/reviews/" + filename);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu ảnh đánh giá");
            }
        }
        return urls;
    }

    private void registerRollbackDelete(Path path) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // The database rollback remains authoritative; an orphan cleanup can run later.
                    }
                }
            }
        });
    }

    private Path resolveReviewUploadDir() {
        Path[] candidates = {
                Paths.get("..", "frontend", "public", "images", "reviews"),
                Paths.get("frontend", "public", "images", "reviews")
        };
        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            Path frontendDir = absolute.getParent().getParent().getParent();
            if (Files.exists(frontendDir)) return absolute;
        }
        return candidates[0].toAbsolutePath().normalize();
    }
}
