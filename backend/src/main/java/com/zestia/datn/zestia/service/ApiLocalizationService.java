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
            Map.entry("Váy dạ hội", "Evening dresses & gowns"),
            Map.entry("Váy cưới", "Bridal & wedding gowns"),
            Map.entry("Váy học sinh", "School dresses"),
            Map.entry("Váy công sở", "Office & tailored dresses"),
            Map.entry("Váy dự tiệc", "Party & cocktail dresses"),
            Map.entry("Áo thời trang", "Fashion tops & blouses"),
            Map.entry("Quần & Jeans", "Pants & Denim"),
            Map.entry("Váy & Đầm", "Dresses & Skirts"),
            Map.entry("Phụ kiện thời trang", "Fashion accessories"),
            Map.entry("Trang phục công sở", "Workwear & tailoring"),
            Map.entry("Trang phục dự tiệc", "Eveningwear & cocktail dress"),
            Map.entry("Áo khoác & Blazer", "Jackets & Blazers"),
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
            Map.entry("Đang giao", "Out for delivery"),
            Map.entry("Giao thành công", "Delivered"),
            Map.entry("Giao thất bại", "Delivery failed"),
            Map.entry("Đã hủy", "Cancelled"),
            Map.entry("Đã thanh toán", "Paid"),
            Map.entry("Chưa thanh toán", "Unpaid"),
            Map.entry("Thanh toán thất bại", "Payment failed"),
            Map.entry("Mua trực tiếp tại cửa hàng", "In-store purchase"),
            Map.entry("Tiền mặt", "Cash"),
            Map.entry("Chuyển khoản", "Bank transfer"),
            Map.entry("Áp dụng mã thành công", "Voucher applied successfully"),
            Map.entry("Chưa có voucher phù hợp", "No eligible voucher available"),
            Map.entry("Mã giảm giá không tồn tại", "Voucher code does not exist"),
            Map.entry("Mã giảm giá đã ngừng hoạt động", "This voucher has been deactivated"),
            Map.entry("Mã giảm giá đã hết lượt sử dụng", "This voucher has no remaining redemptions"),
            Map.entry("Mã giảm giá chưa đến ngày áp dụng", "This voucher is not active yet"),
            Map.entry("Mã giảm giá đã hết hạn", "This voucher code has expired"),
            Map.entry("Vui lòng nhập mã giảm giá", "Please enter a voucher code"),
            Map.entry("Giỏ hàng trống", "Your shopping cart is empty"),
            Map.entry("Sản phẩm không tồn tại", "Product does not exist"),
            Map.entry("Biến thể sản phẩm không tồn tại", "Garment variant does not exist"),
            Map.entry("Phiên đăng nhập không hợp lệ", "Your login session is invalid or has expired"),
            Map.entry("Bạn không có quyền thực hiện thao tác này", "You do not have permission to perform this action"),
            Map.entry("Thiếu thông tin biến thể sản phẩm", "Missing product variant details"),
            Map.entry("Số điện thoại Việt Nam không hợp lệ", "Invalid phone number format"),
            Map.entry("Vui lòng nhập đầy đủ địa chỉ", "Please provide a complete shipping address"),
            Map.entry("Vui lòng chọn đầy đủ tỉnh/thành phố và quận/huyện", "Please select a valid province and district"),
            Map.entry("Điểm đánh giá phải từ 1 đến 5 sao", "Rating must be between 1 and 5 stars"),
            Map.entry("Nội dung đánh giá cần từ 10 đến 2000 ký tự", "Review content must be between 10 and 2000 characters"),
            Map.entry("Chỉ khách đã nhận sản phẩm mới được đánh giá", "Only customers with delivered orders can submit a review"),
            Map.entry("Sản phẩm trong đơn này đã được đánh giá", "This item has already been reviewed for this order"),
            Map.entry("Mỗi đánh giá được tải tối đa 3 ảnh", "You can upload up to 3 photos per review"),
            Map.entry("Mỗi ảnh đánh giá tối đa 5MB", "Each review photo must not exceed 5 MB"),
            Map.entry("Ảnh đánh giá chỉ hỗ trợ JPG hoặc PNG", "Review photos must be in JPG or PNG format"),
            Map.entry("Tệp tải lên không phải ảnh hợp lệ", "Uploaded file is not a valid image"),
            Map.entry("Biến thể đổi không còn đủ tồn kho", "Requested exchange size/color is out of stock"),
            Map.entry("Biến thể muốn đổi không còn đủ tồn kho", "Requested exchange size/color is out of stock"),
            Map.entry("Sản phẩm này đã có hồ sơ đổi hoặc trả hàng", "A return or exchange request has already been filed for this product"),
            Map.entry("Bạn không có quyền đổi trả đơn hàng này", "You are not authorized to return or exchange this order"),
            Map.entry("Vui lòng đăng nhập", "Please sign in to continue"),
            Map.entry("Mã đợt khuyến mãi đã tồn tại", "Promotion campaign code already exists"),
            Map.entry("Tài khoản nhân viên đang bị tạm khóa", "Employee account is currently suspended"),
            Map.entry("Thiếu mã phiên giỏ POS", "Missing POS session identifier"),
            Map.entry("Bạn không có quyền truy cập giỏ POS này", "You do not have access permission for this POS cart"),
            Map.entry("Phiên giỏ POS đã kết thúc", "POS cart session has ended"),
            Map.entry("Phiên giữ hàng đã hết hạn. Vui lòng tạo lại giỏ POS", "Inventory reservation expired. Please recreate the POS cart"),
            Map.entry("Giỏ POS đã thay đổi. Vui lòng tải lại giỏ trước khi thanh toán", "POS cart items have changed. Please refresh before checkout"),
            Map.entry("Voucher trên hóa đơn không khớp với voucher đã giữ", "Cart voucher does not match the reserved voucher"),
            Map.entry("Mỗi biến thể chỉ được giữ tối đa 100 sản phẩm", "Each variant is limited to a maximum of 100 reserved items"),
            Map.entry("Giỏ POS chỉ được tối đa 50 biến thể và 200 sản phẩm", "POS cart supports a maximum of 50 variants and 200 items"),
            Map.entry("Tin nhắn phải từ 2 đến 1000 ký tự", "Message must be between 2 and 1000 characters"),
            Map.entry("Phiên hỗ trợ đã kết thúc", "Support conversation has ended"),
            Map.entry("Phiên hỗ trợ đã được nhân viên khác tiếp nhận", "Support session has been claimed by another representative"),
            Map.entry("Mỗi lần chỉ được xuất tối đa 1 năm dữ liệu", "Export date range cannot exceed 1 year"),
            Map.entry("Mã OTP không hợp lệ hoặc đã hết hạn", "Invalid or expired OTP code"),
            Map.entry("Mật khẩu cũ không chính xác", "Incorrect current password"),
            Map.entry("Mật khẩu mới không được trùng mật khẩu cũ", "New password must differ from current password"),
            Map.entry("Tên đăng nhập hoặc email đã tồn tại", "Username or email is already registered"),
            Map.entry("Tài khoản của bạn đã bị khóa", "Your account has been deactivated"),
            Map.entry("Mã giảm giá không hợp lệ cho đơn hàng này", "Voucher is not applicable to this order"),
            Map.entry("Đơn hàng chưa đạt giá trị tối thiểu", "Order does not meet the minimum purchase requirement"),
            Map.entry("Vui lòng chọn màu và kích cỡ muốn đổi", "Please select the desired color and size for exchange"),
            Map.entry("Vui lòng mô tả tình trạng hàng ít nhất 10 ký tự", "Please describe item condition with at least 10 characters"),
            Map.entry("Vui lòng nhập đầy đủ thông tin nhận tiền hoàn", "Please enter complete bank refund information"),
            Map.entry("Vui lòng tải ít nhất một ảnh tình trạng hàng", "Please upload at least one photo of the item condition")
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
        PHRASES.put("không được vượt quá", "must not exceed");
        PHRASES.put("không đủ tồn kho", "is out of stock");
        PHRASES.put("không thể hoàn tác", "cannot be undone");
        PHRASES.put("Đơn hàng", "Order");
        PHRASES.put("Sản phẩm", "Product");
        PHRASES.put("Biến thể", "Garment variant");
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
