package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerDataService {
    private static final int MAX_CART_LINES = 50;
    private static final int MAX_QUANTITY_PER_LINE = 100;
    private static final int MAX_TOTAL_QUANTITY = 200;

    private final CurrentCustomerService currentCustomerService;
    private final KhachHangRepository customerRepo;
    private final GioHangRepository cartRepo;
    private final GioHangChiTietRepository cartItemRepo;
    private final SanPhamYeuThichRepository wishlistRepo;
    private final LichSuXemRepository recentViewRepo;
    private final SanPhamRepository productRepo;
    private final SanPhamChiTietRepository variantRepo;
    private final AnhRepository imageRepo;
    private final PromotionPricingService promotionPricingService;
    private final LoyaltyService loyaltyService;

    @Transactional(readOnly = true)
    public Map<String, Object> getAll(Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cart", cartItems(customer));
        data.put("wishlistIds", wishlistRepo.findByKhachHangIdOrderByNgayTaoDesc(customer.getId()).stream()
                .map(item -> item.getSanPham().getId())
                .toList());
        data.put("recentProductIds", recentViewRepo.findTop20ByKhachHangIdOrderByNgayXemDesc(customer.getId()).stream()
                .map(item -> item.getSanPham().getId())
                .distinct()
                .toList());
        data.put("loyalty", loyaltyService.toLoyaltySummaryMap(customer));
        return data;
    }

    @Transactional
    public List<Map<String, Object>> replaceCart(Authentication authentication, List<Map<String, Object>> requestedItems) {
        KhachHang customer = currentCustomerService.require(authentication);
        KhachHang lockedCustomer = customerRepo.findByIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        GioHang cart = cartRepo.findByKhachHangIdForUpdate(lockedCustomer.getId()).orElseGet(() -> cartRepo.save(
                GioHang.builder().khachHang(lockedCustomer).ngayTao(LocalDateTime.now()).chiTiets(new ArrayList<>()).build()
        ));

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        int totalQuantity = 0;
        if (requestedItems != null) {
            if (requestedItems.size() > MAX_CART_LINES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng chỉ được tối đa 50 dòng sản phẩm");
            }
            for (Map<String, Object> item : requestedItems) {
                Integer variantId = toInt(item.get("variantId"));
                Integer quantity = toInt(item.get("qty"));
                if (variantId == null || quantity == null || quantity <= 0 || quantity > MAX_QUANTITY_PER_LINE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm trong giỏ không hợp lệ");
                }
                int mergedQuantity = quantities.merge(variantId, quantity, Integer::sum);
                if (mergedQuantity > MAX_QUANTITY_PER_LINE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mỗi biến thể trong giỏ không được vượt quá 100 sản phẩm");
                }
                totalQuantity += quantity;
                if (totalQuantity > MAX_TOTAL_QUANTITY) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng chỉ được tối đa 200 sản phẩm");
                }
            }
        }

        List<GioHangChiTiet> replacements = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            SanPhamChiTiet variant = variantRepo.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Biến thể trong giỏ hàng không tồn tại"));
            if (!isOrderable(variant)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm trong giỏ đã ngừng bán");
            }
            int available = Optional.ofNullable(variant.getSoLuong()).orElse(0);
            int quantity = Math.min(entry.getValue(), available);
            if (quantity <= 0) continue;
            replacements.add(GioHangChiTiet.builder()
                    .gioHang(cart)
                    .sanPhamChiTiet(variant)
                    .soLuong(quantity)
                    .ngayTao(LocalDateTime.now())
                    .build());
        }

        cartItemRepo.deleteByGioHangId(cart.getId());
        cartItemRepo.flush();
        cartItemRepo.saveAll(replacements);
        return cartItems(lockedCustomer);
    }

    @Transactional
    public void addWishlist(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        SanPham product = activeProduct(productId);
        if (wishlistRepo.findByKhachHangIdAndSanPhamId(customer.getId(), productId).isEmpty()) {
            wishlistRepo.save(SanPhamYeuThich.builder()
                    .khachHang(customer)
                    .sanPham(product)
                    .ngayTao(LocalDateTime.now())
                    .build());
        }
    }

    @Transactional
    public void removeWishlist(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        wishlistRepo.deleteByKhachHangIdAndSanPhamId(customer.getId(), productId);
    }

    @Transactional
    public void recordView(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        SanPham product = activeProduct(productId);
        LichSuXem view = recentViewRepo.findByKhachHangIdAndSanPhamId(customer.getId(), productId)
                .orElseGet(() -> LichSuXem.builder().khachHang(customer).sanPham(product).build());
        view.setNgayXem(LocalDateTime.now());
        recentViewRepo.save(view);
    }

    private List<Map<String, Object>> cartItems(KhachHang customer) {
        Optional<GioHang> cart = cartRepo.findByKhachHangId(customer.getId());
        if (cart.isEmpty()) return List.of();
        List<GioHangChiTiet> items = cartItemRepo.findByGioHangId(cart.get().getId());
        List<Integer> productIds = items.stream()
                .map(item -> item.getSanPhamChiTiet().getSanPham().getId())
                .distinct()
                .toList();
        List<Anh> activeImages = productIds.isEmpty()
                ? List.of()
                : imageRepo.findBySanPhamIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1);
        Map<Integer, String> firstImages = activeImages.stream()
                .collect(java.util.stream.Collectors.toMap(
                        image -> image.getSanPham().getId(),
                        Anh::getAnhUrl,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return items.stream()
                .map(item -> cartItemMap(item, firstImages))
                .toList();
    }

    private Map<String, Object> cartItemMap(GioHangChiTiet item, Map<Integer, String> firstImages) {
        SanPhamChiTiet variant = item.getSanPhamChiTiet();
        SanPham product = variant.getSanPham();
        PromotionPricingService.PriceQuote quote = promotionPricingService.quote(variant);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", "variant-" + variant.getId());
        map.put("variantId", variant.getId());
        map.put("productId", product.getId());
        map.put("name", product.getTenSanPham());
        map.put("tenSanPham", product.getTenSanPham());
        map.put("size", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
        map.put("color", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
        map.put("price", quote.effectivePrice());
        map.put("originalPrice", quote.discounted() ? quote.basePrice() : null);
        map.put("promotionActive", quote.discounted());
        map.put("campaign", quote.campaignName());
        map.put("campaignCode", quote.campaignCode());
        map.put("qty", item.getSoLuong());
        map.put("maxQty", Optional.ofNullable(variant.getSoLuong()).orElse(0));
        map.put("image", variant.getAnhUrl() != null ? variant.getAnhUrl() : firstImages.get(product.getId()));
        return map;
    }

    private SanPham activeProduct(Integer productId) {
        SanPham product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        if (product.getTrangThai() == null || product.getTrangThai() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm đã ngừng bán");
        }
        return product;
    }

    private boolean isOrderable(SanPhamChiTiet variant) {
        return variant.getSanPham() != null
                && variant.getMauSac() != null
                && variant.getKichThuoc() != null
                && variant.getSanPham().getTrangThai() != null
                && variant.getSanPham().getTrangThai() == 1
                && Objects.equals(variant.getTrangThai(), (byte) 1)
                && Objects.equals(variant.getMauSac().getTrangThai(), (byte) 1)
                && Objects.equals(variant.getKichThuoc().getTrangThai(), (byte) 1);
    }

    private Integer toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value != null ? Integer.parseInt(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
