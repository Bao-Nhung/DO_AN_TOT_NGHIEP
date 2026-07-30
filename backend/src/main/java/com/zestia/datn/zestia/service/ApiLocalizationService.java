package com.zestia.datn.zestia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ApiLocalizationService {
    private static final Map<String, String> EXACT = Map.ofEntries(
            Map.entry("Váy truyền thống", "Traditional dresses"),
            Map.entry("Váy cách tân", "Modernized traditional dresses"),
            Map.entry("Váy dạ hội", "Evening dresses"),
            Map.entry("Váy cưới", "Wedding dresses"),
            Map.entry("Váy học sinh", "School dresses"),
            Map.entry("Váy công sở", "Office dresses"),
            Map.entry("Váy dự tiệc", "Party dresses"),
            Map.entry("Lụa tơ tằm", "Mulberry silk"),
            Map.entry("Gấm", "Brocade"),
            Map.entry("Voan", "Chiffon"),
            Map.entry("Cotton", "Cotton"),
            Map.entry("Đũi", "Linen"),
            Map.entry("Nhung", "Velvet"),
            Map.entry("Đỏ", "Red"),
            Map.entry("Xanh Navy", "Navy blue"),
            Map.entry("Vàng", "Gold"),
            Map.entry("Trắng", "White"),
            Map.entry("Đen", "Black"),
            Map.entry("Hồng", "Pink"),
            Map.entry("Tím", "Purple"),
            Map.entry("Xanh Lá", "Green"),
            Map.entry("Small - Nhỏ", "Small"),
            Map.entry("Medium - Vừa", "Medium"),
            Map.entry("Large - Lớn", "Large"),
            Map.entry("Đang xử lý", "Processing"),
            Map.entry("Chờ xử lý", "Pending"),
            Map.entry("Đã xác nhận", "Confirmed"),
            Map.entry("Đang giao", "Shipping"),
            Map.entry("Giao thành công", "Delivered"),
            Map.entry("Giao thất bại", "Delivery failed"),
            Map.entry("Đã hủy", "Cancelled"),
            Map.entry("Đã thanh toán", "Paid"),
            Map.entry("Chưa thanh toán", "Unpaid"),
            Map.entry("Thanh toán thất bại", "Payment failed"),
            Map.entry("Mua trực tiếp tại cửa hàng", "Purchased directly at the store"),
            Map.entry("Tiền mặt", "Cash"),
            Map.entry("Chuyển khoản", "Bank transfer"),
            Map.entry("Áp dụng mã thành công", "Voucher applied successfully"),
            Map.entry("Chưa có voucher phù hợp", "No eligible voucher is available"),
            Map.entry("Mã giảm giá không tồn tại", "Voucher code does not exist"),
            Map.entry("Mã giảm giá đã ngừng hoạt động", "This voucher has been disabled"),
            Map.entry("Mã giảm giá đã hết lượt sử dụng", "This voucher has no remaining uses"),
            Map.entry("Mã giảm giá chưa đến ngày áp dụng", "This voucher is not active yet"),
            Map.entry("Mã giảm giá đã hết hạn", "This voucher has expired"),
            Map.entry("Vui lòng nhập mã giảm giá", "Please enter a voucher code"),
            Map.entry("Giỏ hàng trống", "Your cart is empty"),
            Map.entry("Sản phẩm không tồn tại", "Product does not exist"),
            Map.entry("Biến thể sản phẩm không tồn tại", "Product variant does not exist"),
            Map.entry("Phiên đăng nhập không hợp lệ", "Your login session is invalid"),
            Map.entry("Bạn không có quyền thực hiện thao tác này", "You do not have permission to perform this action")
    );

    private static final Map<String, String> PHRASES = new LinkedHashMap<>();
    private final Map<String, String> generatedTranslations;

    static {
        PHRASES.put("Vui lòng", "Please");
        PHRASES.put("Không thể", "Unable to");
        PHRASES.put("không hợp lệ", "is invalid");
        PHRASES.put("không tồn tại", "does not exist");
        PHRASES.put("đã hết hạn", "has expired");
        PHRASES.put("đã ngừng hoạt động", "has been disabled");
        PHRASES.put("Đơn hàng", "Order");
        PHRASES.put("Sản phẩm", "Product");
        PHRASES.put("Khách hàng", "Customer");
        PHRASES.put("Nhân viên", "Employee");
        PHRASES.put("số lượng", "quantity");
        PHRASES.put("màu sắc", "color");
        PHRASES.put("kích thước", "size");
        PHRASES.put("thanh toán", "payment");
        PHRASES.put("đăng nhập", "login");
    }

    public ApiLocalizationService(ObjectMapper objectMapper) {
        Map<String, String> loaded = Collections.emptyMap();
        try (InputStream stream = ApiLocalizationService.class
                .getResourceAsStream("/i18n/auto-en.json")) {
            if (stream != null) {
                loaded = objectMapper.readValue(stream, new TypeReference<>() {});
            }
        } catch (Exception ignored) {
            // Manual translations and phrase fallbacks remain available.
        }
        generatedTranslations = Map.copyOf(loaded);
    }

    public String translate(String source) {
        if (source == null || source.isBlank()) return source;
        String normalized = source.trim();
        String exact = findKnownTranslation(normalized);
        if (exact != null) return exact;
        String translated = source;
        for (Map.Entry<String, String> entry : PHRASES.entrySet()) {
            translated = translated.replace(entry.getKey(), entry.getValue());
        }
        return translated;
    }

    public String translateKnown(String source) {
        if (source == null || source.isBlank()) return source;
        String exact = findKnownTranslation(source.trim());
        return exact != null ? exact : source;
    }

    private String findKnownTranslation(String normalized) {
        String exact = EXACT.get(normalized);
        return exact != null ? exact : generatedTranslations.get(normalized);
    }
}
