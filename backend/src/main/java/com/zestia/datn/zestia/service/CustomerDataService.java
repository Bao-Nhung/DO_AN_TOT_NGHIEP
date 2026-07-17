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
    private final CurrentCustomerService currentCustomerService;
    private final GioHangRepository cartRepo;
    private final GioHangChiTietRepository cartItemRepo;
    private final SanPhamYeuThichRepository wishlistRepo;
    private final LichSuXemRepository recentViewRepo;
    private final VayRepository productRepo;
    private final VayChiTietRepository variantRepo;
    private final AnhRepository imageRepo;
    private final PromotionPricingService promotionPricingService;

    @Transactional(readOnly = true)
    public Map<String, Object> getAll(Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cart", cartItems(customer));
        data.put("wishlistIds", wishlistRepo.findByKhachHangIdOrderByNgayTaoDesc(customer.getId()).stream()
                .map(item -> item.getVay().getId())
                .toList());
        data.put("recentProductIds", recentViewRepo.findTop20ByKhachHangIdOrderByNgayXemDesc(customer.getId()).stream()
                .map(item -> item.getVay().getId())
                .distinct()
                .toList());
        return data;
    }

    @Transactional
    public List<Map<String, Object>> replaceCart(Authentication authentication, List<Map<String, Object>> requestedItems) {
        KhachHang customer = currentCustomerService.require(authentication);
        GioHang cart = cartRepo.findByKhachHangIdForUpdate(customer.getId()).orElseGet(() -> cartRepo.save(
                GioHang.builder().khachHang(customer).ngayTao(LocalDateTime.now()).chiTiets(new ArrayList<>()).build()
        ));

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        if (requestedItems != null) {
            for (Map<String, Object> item : requestedItems) {
                Integer variantId = toInt(item.get("variantId"));
                Integer quantity = toInt(item.get("qty"));
                if (variantId == null || quantity == null || quantity <= 0 || quantity > 100) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm trong giỏ không hợp lệ");
                }
                quantities.merge(variantId, quantity, Integer::sum);
            }
        }

        List<GioHangChiTiet> replacements = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            VayChiTiet variant = variantRepo.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Biến thể trong giỏ hàng không tồn tại"));
            if (!isOrderable(variant)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm trong giỏ đã ngừng bán");
            }
            int available = Optional.ofNullable(variant.getSoLuong()).orElse(0);
            int quantity = Math.min(entry.getValue(), available);
            if (quantity <= 0) continue;
            replacements.add(GioHangChiTiet.builder()
                    .gioHang(cart)
                    .vayChiTiet(variant)
                    .soLuong(quantity)
                    .ngayTao(LocalDateTime.now())
                    .build());
        }

        cartItemRepo.deleteByGioHangId(cart.getId());
        cartItemRepo.flush();
        cartItemRepo.saveAll(replacements);
        return cartItems(customer);
    }

    @Transactional
    public void addWishlist(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        Vay product = activeProduct(productId);
        if (wishlistRepo.findByKhachHangIdAndVayId(customer.getId(), productId).isEmpty()) {
            wishlistRepo.save(SanPhamYeuThich.builder()
                    .khachHang(customer)
                    .vay(product)
                    .ngayTao(LocalDateTime.now())
                    .build());
        }
    }

    @Transactional
    public void removeWishlist(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        wishlistRepo.deleteByKhachHangIdAndVayId(customer.getId(), productId);
    }

    @Transactional
    public void recordView(Authentication authentication, Integer productId) {
        KhachHang customer = currentCustomerService.require(authentication);
        Vay product = activeProduct(productId);
        LichSuXem view = recentViewRepo.findByKhachHangIdAndVayId(customer.getId(), productId)
                .orElseGet(() -> LichSuXem.builder().khachHang(customer).vay(product).build());
        view.setNgayXem(LocalDateTime.now());
        recentViewRepo.save(view);
    }

    private List<Map<String, Object>> cartItems(KhachHang customer) {
        Optional<GioHang> cart = cartRepo.findByKhachHangId(customer.getId());
        if (cart.isEmpty()) return List.of();
        List<GioHangChiTiet> items = cartItemRepo.findByGioHangId(cart.get().getId());
        List<Integer> productIds = items.stream()
                .map(item -> item.getVayChiTiet().getVay().getId())
                .distinct()
                .toList();
        List<Anh> activeImages = productIds.isEmpty()
                ? List.of()
                : imageRepo.findByVayIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1);
        Map<Integer, String> firstImages = activeImages.stream()
                .collect(java.util.stream.Collectors.toMap(
                        image -> image.getVay().getId(),
                        Anh::getAnhUrl,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return items.stream()
                .map(item -> cartItemMap(item, firstImages))
                .toList();
    }

    private Map<String, Object> cartItemMap(GioHangChiTiet item, Map<Integer, String> firstImages) {
        VayChiTiet variant = item.getVayChiTiet();
        Vay product = variant.getVay();
        PromotionPricingService.PriceQuote quote = promotionPricingService.quote(variant);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", "variant-" + variant.getId());
        map.put("variantId", variant.getId());
        map.put("productId", product.getId());
        map.put("name", product.getTenVay());
        map.put("size", variant.getKichThuoc() != null ? variant.getKichThuoc().getTenKichThuoc() : null);
        map.put("color", variant.getMauSac() != null ? variant.getMauSac().getTenMauSac() : null);
        map.put("price", quote.effectivePrice());
        map.put("qty", item.getSoLuong());
        map.put("maxQty", Optional.ofNullable(variant.getSoLuong()).orElse(0));
        map.put("image", variant.getAnhUrl() != null ? variant.getAnhUrl() : firstImages.get(product.getId()));
        return map;
    }

    private Vay activeProduct(Integer productId) {
        Vay product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        if (product.getTrangThai() == null || product.getTrangThai() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm đã ngừng bán");
        }
        return product;
    }

    private boolean isOrderable(VayChiTiet variant) {
        return variant.getVay() != null
                && variant.getVay().getTrangThai() != null
                && variant.getVay().getTrangThai() == 1
                && (variant.getTrangThai() == null || variant.getTrangThai() == 1);
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
