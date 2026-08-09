package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import com.zestia.datn.zestia.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private static final byte STATUS_PENDING = 0;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("0[35789]\\d{8}");
    private static final int MAX_ORDER_LINES = 50;
    private static final int MAX_QUANTITY_PER_LINE = 100;
    private static final int MAX_TOTAL_QUANTITY = 200;

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final SanPhamChiTietRepository vayCtRepo;
    private final KhachHangRepository khachHangRepo;
    private final LichSuThanhToanRepository lichSuRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final NhanVienRepository nhanVienRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final ShippingFeeService shippingFeeService;
    private final CustomerIdentityService customerIdentityService;
    private final CustomerAddressService customerAddressService;
    private final PromotionPricingService promotionPricingService;
    private final InventoryMovementService inventoryMovementService;
    private final PosReservationService posReservationService;
    private final VoucherApplicationService voucherApplicationService;
    private final RequestRateLimiter rateLimiter;

    @PostMapping("/create-order")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader,
                                         HttpServletRequest request) {

        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu đơn hàng không hợp lệ"));
        }
        if (!rateLimiter.tryAcquire("create-order", clientIp(request), 20, 5 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã tạo quá nhiều đơn trong thời gian ngắn. Vui lòng thử lại sau"
            ));
        }

        String hoTen = cleanString(body.get("hoTen"));
        if (hoTen == null) {
            hoTen = cleanString(body.get("tenKhachHang"));
        }
        String soDienThoai = cleanString(body.get("soDienThoai"));
        String emailKhachHang = cleanString(body.get("email"));
        String diaChi = cleanString(body.get("diaChi"));
        String ghiChu = cleanString(body.get("ghiChu"));
        String hinhThuc = cleanString(body.get("hinhThucThanhToan"));
        String checkoutRequestId = cleanString(body.get("checkoutRequestId"));
        if (checkoutRequestId != null && checkoutRequestId.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã yêu cầu thanh toán không hợp lệ"));
        }

        Object rawItems = body.get("items");
        if (!(rawItems instanceof List<?> itemList) || itemList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
        }
        if (itemList.size() > MAX_ORDER_LINES) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mỗi đơn hàng chỉ được tối đa 50 dòng sản phẩm"));
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object rawItem : itemList) {
            if (!(rawItem instanceof Map<?, ?> source)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Dòng sản phẩm không hợp lệ"));
            }
            Map<String, Object> item = new LinkedHashMap<>();
            source.forEach((key, value) -> {
                if (key instanceof String stringKey) item.put(stringKey, value);
            });
            items.add(item);
        }
        items.sort(Comparator.comparingInt(item -> {
            Integer variantId = firstInt(item, "variantId", "sanPhamChiTietId", "idSanPhamChiTiet", "vayChiTietId", "idVayChiTiet");
            if (variantId != null) return variantId;
            Integer productId = firstInt(item, "productId", "idSanPham", "sanPhamId", "idVay", "vayId", "id");
            return productId != null ? productId : Integer.MAX_VALUE;
        }));

        KhachHang kh = null;
        NhanVien nv = null;
        String tokenRole = null;
        Integer tokenUserId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if (!jwtUtil.isValid(token)) {
                    return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
                }
                String username = jwtUtil.extractUsername(token);
                var claims = jwtUtil.extractClaims(token);
                tokenRole = claims.get("role", String.class);
                tokenUserId = toInt(claims.get("userId"));
                if (isCustomerRole(tokenRole)) {
                    kh = khachHangRepo.findByEmail(username)
                            .or(() -> khachHangRepo.findBySoDienThoai(username))
                            .orElse(null);
                } else if (isStaffRole(tokenRole) && tokenUserId != null) {
                    nv = nhanVienRepo.findById(tokenUserId).orElse(null);
                }
            } catch (Exception ignored) {
                return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
            }
        }

        Integer requestedShippingType = toInt(body.get("hinhThucNhanHang"));
        Integer requestedStatus = toInt(body.get("trangThai"));
        boolean requestedOffline = requestedShippingType != null && requestedShippingType == 0;
        boolean requestedPaid = isTruthy(body.get("daThanhToan")) || (requestedStatus != null && requestedStatus >= 3);
        if ((requestedOffline || requestedPaid) && nv == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Chi nhan vien hoac admin moi duoc tao don ban tai quay"));
        }
        if (requestedPaid && !requestedOffline) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Trạng thái thanh toán tại quầy phải đi cùng hình thức nhận hàng trực tiếp"
            ));
        }
        boolean staffDirectSale = nv != null && requestedOffline;
        String posReservationToken = cleanString(body.get("posReservationToken"));
        if (!staffDirectSale && posReservationToken != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Phiên giữ hàng POS chỉ được dùng cho đơn bán tại quầy"
            ));
        }
        if (nv != null && nv.getTinhTrangLamViec() != null && nv.getTinhTrangLamViec() != 1) {
            return ResponseEntity.status(403).body(Map.of("error", "Tài khoản nhân viên đang bị tạm khóa"));
        }

        Integer nhanVienId = toInt(body.get("nhanVienId"));
        if (staffDirectSale && nhanVienId != null && !Objects.equals(nhanVienId, tokenUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Nhan vien khong khop voi token dang nhap"));
        }

        if (hoTen == null || hoTen.length() < 2 || hoTen.length() > 150) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập họ tên hợp lệ"));
        }
        if (soDienThoai != null) {
            soDienThoai = normalizePhone(soDienThoai);
        }
        if (!PHONE_PATTERN.matcher(Objects.toString(soDienThoai, "")).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập số điện thoại Việt Nam hợp lệ"));
        }
        if (!staffDirectSale && (diaChi == null || diaChi.length() < 5 || diaChi.length() > 500)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đầy đủ địa chỉ giao hàng"));
        }
        if (ghiChu != null && ghiChu.length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ghi chú không được vượt quá 2000 ký tự"));
        }

        if (emailKhachHang == null && kh != null && kh.getEmail() != null && !kh.getEmail().isBlank()) {
            emailKhachHang = kh.getEmail().trim();
        }
        if (!staffDirectSale) {
            if (emailKhachHang == null || !EMAIL_PATTERN.matcher(emailKhachHang).matches()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui long nhap email hop le de nhan hoa don"));
            }
        } else if (emailKhachHang != null && !EMAIL_PATTERN.matcher(emailKhachHang).matches()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email khach hang khong hop le"));
        }

        String paymentMethod = normalizePaymentMethod(hinhThuc, staffDirectSale);
        if (paymentMethod == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phương thức thanh toán không hợp lệ"));
        }

        if (checkoutRequestId != null) {
            Optional<HoaDon> existing = hoaDonRepo.findByMaYeuCau(checkoutRequestId);
            if (existing.isPresent()) {
                HoaDon order = existing.get();
                if (!matchesExistingOrderOwner(order, kh, soDienThoai, emailKhachHang, nv)) {
                    return ResponseEntity.status(409).body(Map.of("error", "Mã yêu cầu đã được dùng cho giao dịch khác"));
                }
                return ResponseEntity.ok(orderResponse(order, BigDecimal.ZERO));
            }
        }

        PosReservationService.CheckoutReservation posReservation = null;
        if (staffDirectSale && posReservationToken != null) {
            Map<Integer, Integer> requestedQuantities = new LinkedHashMap<>();
            for (Map<String, Object> item : items) {
                Integer variantId = firstInt(item, "variantId", "sanPhamChiTietId", "idSanPhamChiTiet", "vayChiTietId", "idVayChiTiet");
                Integer quantity = firstInt(item, "qty", "quantity", "soLuong");
                if (variantId == null || quantity == null || quantity <= 0) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Giỏ POS đã giữ hàng phải gửi đầy đủ variantId và số lượng"
                    ));
                }
                requestedQuantities.merge(variantId, quantity, Integer::sum);
            }
            try {
                posReservation = posReservationService.prepareCheckout(
                        posReservationToken,
                        tokenUserId,
                        requestedQuantities,
                        cleanString(body.get("maGiamGia"))
                );
            } catch (IllegalArgumentException exception) {
                return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
            } catch (IllegalStateException exception) {
                return ResponseEntity.status(409).body(Map.of("error", exception.getMessage()));
            }
        }

        String maHoaDon = generateOrderCode();

        BigDecimal tamTinh = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        Map<Integer, SanPhamChiTiet> plannedVariants = new LinkedHashMap<>();
        Map<Integer, Integer> stockDeductions = new LinkedHashMap<>();
        int totalQuantity = 0;

        for (Map<String, Object> item : items) {
            Integer productId = firstInt(item, "productId", "idSanPham", "sanPhamId", "idVay", "vayId");
            if (productId == null) productId = toInt(item.get("id"));
            
            Integer variantId = firstInt(item, "variantId", "sanPhamChiTietId", "idSanPhamChiTiet", "vayChiTietId", "idVayChiTiet");
            Integer qty = firstInt(item, "qty", "quantity", "soLuong");
            if (qty == null || qty <= 0 || qty > MAX_QUANTITY_PER_LINE) {
                return ResponseEntity.badRequest().body(Map.of("error", "So luong san pham khong hop le"));
            }
            totalQuantity += qty;
            if (totalQuantity > MAX_TOTAL_QUANTITY) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mỗi đơn hàng chỉ được tối đa 200 sản phẩm"));
            }

            SanPhamChiTiet selectedById = null;
            if (variantId != null) {
                selectedById = vayCtRepo.findByIdForUpdate(variantId).orElse(null);
                if (!isOrderableVariant(selectedById)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bien the san pham khong ton tai hoac da ngung ban"));
                }
                if (productId != null && !Objects.equals(productId, selectedById.getSanPham().getId())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bien the khong thuoc san pham da chon"));
                }
                productId = selectedById.getSanPham().getId();
            }
            if (productId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thieu thong tin san pham"));
            }

            var variants = selectedById != null
                    ? List.of(selectedById)
                    : vayCtRepo.findBySanPhamIdForUpdate(productId).stream()
                            .filter(PaymentController::isOrderableVariant)
                            .toList();
            if (variants.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "San pham khong co bien the dang ban"));
            }

            SanPhamChiTiet variant;
            String size = cleanString(item.get("size"));
            String color = cleanString(item.get("color"));
            variant = selectedById;

            if (variant == null) {
                if (size == null || color == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Vui long chon day du mau sac va kich thuoc"));
                }
                for (SanPhamChiTiet v : variants) {
                    boolean matchSize = size == null || (v.getKichThuoc() != null && size.equalsIgnoreCase(v.getKichThuoc().getTenKichThuoc()));
                    boolean matchColor = color == null || (v.getMauSac() != null && color.equalsIgnoreCase(v.getMauSac().getTenMauSac()));
                    
                    if (matchSize && matchColor) {
                        variant = v;
                        break;
                    }
                }
            }
            if (variant == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bien the mau/size khong ton tai hoac da ngung ban"));
            }

            int soLuongKho = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            int reservedQuantity = posReservation != null
                    ? posReservation.quantities().getOrDefault(variant.getId(), 0)
                    : 0;
            int plannedQty = stockDeductions.getOrDefault(variant.getId(), 0) + qty;
            if (soLuongKho + reservedQuantity < plannedQty) {
                String tenSp = variant.getSanPham() != null ? variant.getSanPham().getTenSanPham() : "Sản phẩm";
                String thongTinBT = (color != null ? " - Màu " + color : "") + (size != null ? " - Size " + size : "");
                return ResponseEntity.badRequest().body(Map.of("error", 
                    tenSp + thongTinBT + " không đủ số lượng. Chỉ còn "
                            + (soLuongKho + reservedQuantity) + " sản phẩm."));
            }
            
            plannedVariants.put(variant.getId(), variant);
            stockDeductions.put(variant.getId(), plannedQty);

            PromotionPricingService.PriceQuote priceQuote = promotionPricingService.quote(variant);
            BigDecimal price = posReservation != null
                    ? posReservation.prices().get(variant.getId())
                    : priceQuote.effectivePrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Giá sản phẩm trong phiên POS không còn hợp lệ. Vui lòng tạo lại giỏ"
                ));
            }
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
            tamTinh = tamTinh.add(lineTotal);

            BigDecimal phanTramGiam = priceQuote.discountPercent();

            BigDecimal giaNhap = variant.getGiaNhap();

            chiTietList.add(HoaDonChiTiet.builder()
                    .sanPhamChiTiet(variant)
                    .soLuong(qty)
                    .donGia(price)
                    .giaNhap(giaNhap)
                    .phanTramGiam(phanTramGiam)
                    .build());
        }

        if (chiTietList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có sản phẩm hợp lệ"));
        }

        BigDecimal phiVanChuyen;
        try {
            phiVanChuyen = shippingFeeService.calculate(
                    staffDirectSale,
                    tamTinh,
                    body.get("tinhThanhCode"),
                    body.get("quanHuyenCode")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        // Áp dụng voucher (mã giảm giá) nếu có
        GiamGia voucher = null;
        BigDecimal giamGia = BigDecimal.ZERO;
        String maGiamGia = (String) body.get("maGiamGia");
        if (posReservation != null) {
            voucher = posReservation.voucher();
            if (voucher != null) {
                var evaluation = voucherApplicationService.evaluate(voucher, tamTinh, 1);
                if (!evaluation.valid()) {
                    return ResponseEntity.badRequest().body(Map.of("error", evaluation.message()));
                }
                giamGia = evaluation.discount();
            }
        } else if (maGiamGia != null && !maGiamGia.isBlank()) {
            var evaluation = voucherApplicationService.validate(maGiamGia.trim(), tamTinh, true);
            if (!evaluation.valid()) {
                return ResponseEntity.badRequest().body(Map.of("error", evaluation.message()));
            }
            voucher = evaluation.voucher();
            giamGia = evaluation.discount();
        }

        BigDecimal tongTien = tamTinh.add(phiVanChuyen).subtract(giamGia);
        if (tongTien.compareTo(BigDecimal.ZERO) < 0) tongTien = BigDecimal.ZERO;

        if (staffDirectSale && "Tiền mặt".equals(paymentMethod)) {
            BigDecimal cashReceived = parseMoney(body.get("tienKhachDua"));
            if (cashReceived == null || cashReceived.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Số tiền khách đưa không hợp lệ"
                ));
            }
            if (cashReceived.compareTo(tongTien) < 0) {
                BigDecimal missing = tongTien.subtract(cashReceived);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Số tiền khách đưa chưa đủ. Còn thiếu " + missing.toPlainString() + "đ"
                ));
            }
        }

        try {
            kh = customerIdentityService.resolveForOrder(kh, hoTen, soDienThoai, emailKhachHang);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }

        for (Map.Entry<Integer, Integer> entry : stockDeductions.entrySet()) {
            SanPhamChiTiet variant = plannedVariants.get(entry.getKey());
            int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            int heldQuantity = posReservation != null
                    ? posReservation.quantities().getOrDefault(entry.getKey(), 0)
                    : 0;
            int quantityToDeductNow = entry.getValue() - heldQuantity;
            int afterStock = currentStock - quantityToDeductNow;
            variant.setSoLuong(afterStock);
            vayCtRepo.save(variant);
            inventoryMovementService.record(
                    variant, currentStock, afterStock,
                    staffDirectSale ? "BAN_TAI_QUAY" : "GIU_TON_CHECKOUT",
                    maHoaDon,
                    nv != null ? nv.getHoVaTen() : hoTen,
                    "Trừ tồn khi tạo đơn hàng"
            );
        }

        byte trangThai = staffDirectSale ? (byte) 4 : STATUS_PENDING;
        boolean daThanhToan = staffDirectSale;
        byte hinhThucNhanHang = (byte) (staffDirectSale ? 0 : 1);

        Boolean yeuCauVat = Boolean.TRUE.equals(body.get("yeuCauVat")) || Boolean.TRUE.equals(body.get("isVatRequested"));
        String tenCongTyVat = cleanString(body.get("tenCongTyVat"));
        String maSoThueVat = cleanString(body.get("maSoThueVat"));
        String emailVat = cleanString(body.get("emailVat"));
        String diaChiVat = cleanString(body.get("diaChiVat"));

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(maHoaDon)
                .maYeuCau(checkoutRequestId)
                .khachHang(kh)
                .nhanVien(nv)
                .giamGia(voucher)
                .tongTien(tongTien)
                .phiVanChuyen(phiVanChuyen)
                .giamGiaVoucher(giamGia)
                .hinhThucNhanHang(hinhThucNhanHang)
                .diaChiGiaoHang(staffDirectSale ? "Mua trực tiếp tại cửa hàng" : diaChi)
                .hinhThucThanhToan(paymentMethod)
                .trangThai(trangThai)
                .daThanhToan(daThanhToan)
                .daHoanTonKho(false)
                .ghiChu(ghiChu)
                .ngayTao(LocalDateTime.now())
                .tenKhachHang(hoTen)
                .soDienThoai(soDienThoai)
                .emailKhachHang(emailKhachHang)
                .yeuCauVat(yeuCauVat)
                .tenCongTyVat(yeuCauVat ? tenCongTyVat : null)
                .maSoThueVat(yeuCauVat ? maSoThueVat : null)
                .emailVat(yeuCauVat ? emailVat : null)
                .diaChiVat(yeuCauVat ? diaChiVat : null)
                .trangThaiVat(yeuCauVat ? "CHO_PHAT_HANH" : null)
                .build();

        hoaDon = hoaDonRepo.save(hoaDon);

        if (!staffDirectSale) {
            customerAddressService.saveCheckoutAddress(kh, body);
        }

        for (HoaDonChiTiet ct : chiTietList) {
            ct.setHoaDon(hoaDon);
        }
        hoaDonCtRepo.saveAll(chiTietList);

        if (posReservation == null
                && voucher != null
                && voucher.getSoLuong() != null
                && voucher.getSoLuong() > 0) {
            voucher.setSoLuong(voucher.getSoLuong() - 1);
            giamGiaRepo.save(voucher);
        }

        if (posReservation != null) {
            posReservationService.completeCheckout(posReservation.token(), tokenUserId, hoaDon);
        }

        if (daThanhToan) {
            lichSuRepo.save(LichSuThanhToan.builder()
                    .hoaDon(hoaDon)
                    .soTien(tongTien)
                    .phuongThuc(hoaDon.getHinhThucThanhToan())
                    .maGiaoDich("POS" + System.currentTimeMillis())
                    .trangThai("SUCCESS")
                    .noiDung("Thanh toán tại quầy - " + maHoaDon)
                    .ngayTao(LocalDateTime.now())
                    .build());
        }

        if (staffDirectSale || "COD".equalsIgnoreCase(paymentMethod)) {
            sendOrderConfirmationAfterCommit(hoaDon);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", hoaDon.getId());
        result.put("maHoaDon", maHoaDon);
        result.put("tamTinh", tamTinh);
        result.put("giamGia", giamGia);
        result.put("phiVanChuyen", phiVanChuyen);
        result.put("tongTien", tongTien);
        result.put("hinhThucThanhToan", hoaDon.getHinhThucThanhToan());
        result.put("hinhThucNhanHang", hoaDon.getHinhThucNhanHang());
        result.put("trangThai", hoaDon.getTrangThai());
        result.put("emailKhachHang", hoaDon.getEmailKhachHang());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/apply-voucher")
    public ResponseEntity<?> applyVoucher(@RequestBody Map<String, Object> body) {
        String ma = (String) body.get("maGiamGia");
        BigDecimal tamTinh = parseMoney(body.get("tongTien"));
        if (tamTinh == null || tamTinh.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Tổng tiền không hợp lệ"));
        }
        if (ma == null || ma.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Vui lòng nhập mã giảm giá"));
        }
        var evaluation = voucherApplicationService.validate(ma.trim(), tamTinh, false);
        if (!evaluation.valid()) {
            return ResponseEntity.ok(Map.of("valid", false, "message", evaluation.message()));
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("valid", true);
        res.put("maGiamGia", evaluation.voucher().getMaGiamGia());
        res.put("tenGiamGia", evaluation.voucher().getTenGiamGia());
        res.put("giamGia", evaluation.discount());
        res.put("message", "Áp dụng mã thành công");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/best-voucher")
    public ResponseEntity<?> findBestVoucher(@RequestBody Map<String, Object> body) {
        BigDecimal tamTinh = parseMoney(body.get("tongTien"));
        if (tamTinh == null || tamTinh.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Chưa có voucher phù hợp"));
        }

        var best = voucherApplicationService.findBest(tamTinh);
        if (!best.valid()) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Chưa có voucher phù hợp"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", true);
        response.put("maGiamGia", best.voucher().getMaGiamGia());
        response.put("tenGiamGia", best.voucher().getTenGiamGia());
        response.put("giamGia", best.discount());
        response.put("message", "Đã tự động chọn voucher tiết kiệm nhất");
        return ResponseEntity.ok(response);
    }

    private BigDecimal parseMoney(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Integer id,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id)
                .map(hd -> {
                    ResponseEntity<?> authError = authorizeOrderAccess(hd, authHeader);
                    if (authError != null) return authError;
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", hd.getId());
                    map.put("maHoaDon", hd.getMaHoaDon());
                    map.put("tongTien", hd.getTongTien());
                    map.put("trangThai", hd.getTrangThai());
                    map.put("hinhThucThanhToan", hd.getHinhThucThanhToan());
                    map.put("ngayTao", hd.getNgayTao());
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private static Integer firstInt(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) return null;
        for (String key : keys) {
            Integer value = toInt(map.get(key));
            if (value != null) return value;
        }
        return null;
    }

    private static String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static String cleanString(Object obj) {
        if (obj == null) return null;
        String value = String.valueOf(obj).trim();
        return value.isEmpty() ? null : value;
    }

    private static boolean isOrderableVariant(SanPhamChiTiet variant) {
        return variant != null
                && variant.getSanPham() != null
                && variant.getMauSac() != null
                && variant.getKichThuoc() != null
                && (variant.getTrangThai() == null || variant.getTrangThai() == 1)
                && (variant.getSanPham().getTrangThai() == null || variant.getSanPham().getTrangThai() == 1);
    }

    private String generateOrderCode() {
        String code;
        do {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
            code = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + suffix;
        } while (hoaDonRepo.existsByMaHoaDon(code));
        return code;
    }

    private String normalizePaymentMethod(String method, boolean staffDirectSale) {
        String value = cleanString(method);
        if (value == null) return staffDirectSale ? "Tiền mặt" : "COD";
        String normalized = value.toUpperCase(Locale.ROOT);
        if (staffDirectSale) {
            return switch (normalized) {
                case "TIỀN MẶT" -> "Tiền mặt";
                case "CHUYỂN KHOẢN (VIETQR)" -> "Chuyển khoản (VietQR)";
                case "CHUYỂN KHOẢN (MOMO)", "MOMO" -> "Chuyển khoản (MoMo)";
                case "CHUYỂN KHOẢN (ZALOPAY)", "ZALOPAY" -> "Chuyển khoản (ZaloPay)";
                default -> null;
            };
        }
        return Set.of("COD", "MOMO", "ZALOPAY").contains(normalized) ? normalized : null;
    }

    private boolean matchesExistingOrderOwner(HoaDon order, KhachHang customer, String phone,
                                              String email, NhanVien employee) {
        if (employee != null && order.getNhanVien() != null
                && Objects.equals(employee.getId(), order.getNhanVien().getId())) return true;
        if (customer != null && order.getKhachHang() != null
                && Objects.equals(customer.getId(), order.getKhachHang().getId())) return true;
        return normalizePhone(phone).equals(normalizePhone(order.getSoDienThoai()))
                && email != null
                && order.getEmailKhachHang() != null
                && email.equalsIgnoreCase(order.getEmailKhachHang());
    }

    private Map<String, Object> orderResponse(HoaDon order, BigDecimal fallbackSubtotal) {
        BigDecimal shipping = Optional.ofNullable(order.getPhiVanChuyen()).orElse(BigDecimal.ZERO);
        BigDecimal discount = Optional.ofNullable(order.getGiamGiaVoucher()).orElse(BigDecimal.ZERO);
        BigDecimal total = Optional.ofNullable(order.getTongTien()).orElse(BigDecimal.ZERO);
        BigDecimal subtotal = total.subtract(shipping).add(discount);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = fallbackSubtotal;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getId());
        result.put("maHoaDon", order.getMaHoaDon());
        result.put("tamTinh", subtotal);
        result.put("giamGia", discount);
        result.put("phiVanChuyen", shipping);
        result.put("tongTien", total);
        result.put("hinhThucThanhToan", order.getHinhThucThanhToan());
        result.put("hinhThucNhanHang", order.getHinhThucNhanHang());
        result.put("trangThai", order.getTrangThai());
        result.put("emailKhachHang", order.getEmailKhachHang());
        result.put("idempotent", true);
        return result;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9+]", "");
        return cleaned.startsWith("+84") ? "0" + cleaned.substring(3) : cleaned;
    }

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }

    private static boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(obj));
    }

    private static boolean isCustomerRole(String role) {
        return "KhachHang".equalsIgnoreCase(role);
    }

    private static boolean isStaffRole(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "NhanVien".equalsIgnoreCase(role)
                || "Nhân viên".equalsIgnoreCase(role);
    }

    private void sendOrderConfirmationAfterCommit(HoaDon hoaDon) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendOrderConfirmationEmail(hoaDon);
                }
            });
            return;
        }
        emailService.sendOrderConfirmationEmail(hoaDon);
    }

    private ResponseEntity<?> authorizeOrderAccess(HoaDon hd, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
            }
            var claims = jwtUtil.extractClaims(token);
            String role = claims.get("role", String.class);
            Integer userId = toInt(claims.get("userId"));
            if (isStaffRole(role)) return null;
            if (isCustomerRole(role)
                    && hd.getKhachHang() != null
                    && Objects.equals(hd.getKhachHang().getId(), userId)) {
                return null;
            }
            return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen xem don hang nay"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
        }
    }
}
