package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.OrderInventoryService;
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
    private static final byte STATUS_CONFIRMED = 1;
    private static final byte STATUS_CANCELLED = 5;
    private static final byte STATUS_PAYMENT_FAILED = 7;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final VayChiTietRepository vayCtRepo;
    private final KhachHangRepository khachHangRepo;
    private final LichSuThanhToanRepository lichSuRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final NhanVienRepository nhanVienRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OrderInventoryService orderInventoryService;

    @PostMapping("/create-order")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String hoTen = (String) body.get("hoTen");
        if (hoTen == null) {
            hoTen = (String) body.get("tenKhachHang");
        }
        String soDienThoai = (String) body.get("soDienThoai");
        String emailKhachHang = cleanString(body.get("email"));
        String diaChi = (String) body.get("diaChi");
        String ghiChu = (String) body.get("ghiChu");
        String hinhThuc = (String) body.get("hinhThucThanhToan");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
        }

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
            } catch (Exception ignored) {}
        }

        Integer requestedShippingType = toInt(body.get("hinhThucNhanHang"));
        Integer requestedStatus = toInt(body.get("trangThai"));
        boolean requestedOffline = requestedShippingType != null && requestedShippingType == 0;
        boolean requestedPaid = isTruthy(body.get("daThanhToan")) || (requestedStatus != null && requestedStatus >= 3);
        if ((requestedOffline || requestedPaid) && nv == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Chi nhan vien hoac admin moi duoc tao don ban tai quay"));
        }
        boolean staffDirectSale = nv != null && (requestedOffline || requestedPaid);

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

        String maHoaDon = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                          + String.format("%04d", new Random().nextInt(10000));

        BigDecimal tamTinh = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        Map<Integer, VayChiTiet> plannedVariants = new LinkedHashMap<>();
        Map<Integer, Integer> stockDeductions = new LinkedHashMap<>();

        for (Map<String, Object> item : items) {
            // === THÊM 1 DÒNG ĐỂ ĐỌC ĐƯỢC CẢ "productId" HOẶC "id" TỪ FRONTEND GỬI LÊN ===
            Integer productId = firstInt(item, "productId", "idVay", "vayId");
            if (productId == null) productId = toInt(item.get("id")); // Dòng này là "cứu tinh"
            
            Integer variantId = firstInt(item, "variantId", "vayChiTietId", "idVayChiTiet");
            Integer qty = firstInt(item, "qty", "quantity", "soLuong");
            if (qty == null || qty <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "So luong san pham khong hop le"));
            }

            VayChiTiet selectedById = null;
            if (variantId != null) {
                selectedById = vayCtRepo.findByIdForUpdate(variantId).orElse(null);
                if (!isOrderableVariant(selectedById)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bien the san pham khong ton tai hoac da ngung ban"));
                }
                if (productId != null && !Objects.equals(productId, selectedById.getVay().getId())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Bien the khong thuoc san pham da chon"));
                }
                productId = selectedById.getVay().getId();
            }
            if (productId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thieu thong tin san pham"));
            }

            var variants = vayCtRepo.findByVayIdForUpdate(productId).stream()
                    .filter(PaymentController::isOrderableVariant)
                    .toList();
            if (variants.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "San pham khong co bien the dang ban"));
            }

            // ==========================================
            // TÌM CHÍNH XÁC BIẾN THỂ (KÍCH THƯỚC & MÀU SẮC)
            // ==========================================
            VayChiTiet variant = variants.get(0); // Lấy mặc định nếu không khớp
            String size = cleanString(item.get("size"));
            String color = cleanString(item.get("color"));
            variant = selectedById;

            if (variant == null) {
                if (size == null || color == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Vui long chon day du mau sac va kich thuoc"));
                }
                for (VayChiTiet v : variants) {
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

            // ==========================================
            // KIỂM TRA VÀ TRỪ TỒN KHO THỰC TẾ
            // ==========================================
            int soLuongKho = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            int plannedQty = stockDeductions.getOrDefault(variant.getId(), 0) + qty;
            if (soLuongKho < plannedQty) {
                String tenSp = variant.getVay() != null ? variant.getVay().getTenVay() : "Sản phẩm";
                String thongTinBT = (color != null ? " - Màu " + color : "") + (size != null ? " - Size " + size : "");
                return ResponseEntity.badRequest().body(Map.of("error", 
                    tenSp + thongTinBT + " không đủ số lượng. Chỉ còn " + soLuongKho + " sản phẩm."));
            }
            
            // Tiến hành trừ kho ngay khi đặt hàng
            plannedVariants.put(variant.getId(), variant);
            stockDeductions.put(variant.getId(), plannedQty);
            // ==========================================

            BigDecimal price = variant.getGiaBan();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
            tamTinh = tamTinh.add(lineTotal);

            BigDecimal phanTramGiam = BigDecimal.ZERO;
            if (variant.getGiaBanGoc() != null && variant.getGiaBanGoc().compareTo(variant.getGiaBan()) > 0) {
                phanTramGiam = BigDecimal.ONE.subtract(
                    variant.getGiaBan().divide(variant.getGiaBanGoc(), 4, java.math.RoundingMode.HALF_UP)
                ).multiply(BigDecimal.valueOf(100));
            }

            BigDecimal giaNhap = variant.getGiaNhap() != null
                    ? variant.getGiaNhap()
                    : price.multiply(BigDecimal.valueOf(0.65)).setScale(0, java.math.RoundingMode.HALF_UP);

            chiTietList.add(HoaDonChiTiet.builder()
                    .vayChiTiet(variant)
                    .soLuong(qty)
                    .donGia(price)
                    .giaNhap(giaNhap)
                    .phanTramGiam(phanTramGiam)
                    .build());
        }

        if (chiTietList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có sản phẩm hợp lệ"));
        }

        // Lấy phí vận chuyển từ Frontend gửi lên (Mặc định là 0 nếu không có)
        for (Map.Entry<Integer, Integer> entry : stockDeductions.entrySet()) {
            VayChiTiet variant = plannedVariants.get(entry.getKey());
            int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            variant.setSoLuong(currentStock - entry.getValue());
            vayCtRepo.save(variant);
        }

        BigDecimal phiVanChuyen = BigDecimal.ZERO;
        if (body.get("phiVanChuyen") != null) {
            phiVanChuyen = new BigDecimal(body.get("phiVanChuyen").toString());
        }

        // Áp dụng voucher (mã giảm giá) nếu có
        GiamGia voucher = null;
        BigDecimal giamGia = BigDecimal.ZERO;
        String maGiamGia = (String) body.get("maGiamGia");
        if (maGiamGia != null && !maGiamGia.isBlank()) {
            var vr = validateVoucher(maGiamGia.trim(), tamTinh, true);
            if (vr.valid) {
                voucher = vr.giamGia;
                giamGia = vr.discount;
            }
        }

        // CẬP NHẬT TÍNH TỔNG TIỀN: Tiền hàng + Phí Ship - Giảm giá
        BigDecimal tongTien = tamTinh.add(phiVanChuyen).subtract(giamGia);
        if (tongTien.compareTo(BigDecimal.ZERO) < 0) tongTien = BigDecimal.ZERO;

        // Trạng thái: mặc định 0 (Chờ xử lý). POS gửi trangThai=4 (Hoàn thành - cập nhật theo luồng mới).
        byte trangThai = staffDirectSale ? (byte) 4 : STATUS_PENDING;
        if (staffDirectSale && requestedStatus != null && requestedStatus >= 0 && requestedStatus <= 4) {
            trangThai = requestedStatus.byteValue();
        }

        // Nhân viên (đơn bán tại quầy)
        Integer nhanVienId = toInt(body.get("nhanVienId"));
        if (staffDirectSale && nhanVienId != null && !Objects.equals(nhanVienId, tokenUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Nhan vien khong khop voi token dang nhap"));
        }

        // Đã thanh toán: POS = true; nếu body chỉ định thì theo body
        boolean daThanhToan = staffDirectSale && (body.get("daThanhToan") == null || isTruthy(body.get("daThanhToan")));
        byte hinhThucNhanHang = (byte) (staffDirectSale ? 0 : 1);

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(maHoaDon)
                .khachHang(kh)
                .nhanVien(nv)
                .giamGia(voucher)
                .tongTien(tongTien)
                .phiVanChuyen(phiVanChuyen)
                .giamGiaVoucher(giamGia)
                .hinhThucNhanHang(hinhThucNhanHang)
                .diaChiGiaoHang(diaChi != null ? diaChi : "")
                .hinhThucThanhToan(hinhThuc != null ? hinhThuc : "COD")
                .trangThai(trangThai)
                .daThanhToan(daThanhToan)
                .ghiChu(ghiChu)
                .ngayTao(LocalDateTime.now())
                .tenKhachHang(hoTen)
                .soDienThoai(soDienThoai)
                .emailKhachHang(emailKhachHang)
                .build();

        hoaDon = hoaDonRepo.save(hoaDon);

        for (HoaDonChiTiet ct : chiTietList) {
            ct.setHoaDon(hoaDon);
        }
        hoaDonCtRepo.saveAll(chiTietList);

        // Trừ số lượng voucher đã dùng
        if (voucher != null && voucher.getSoLuong() != null && voucher.getSoLuong() > 0) {
            voucher.setSoLuong(voucher.getSoLuong() - 1);
            giamGiaRepo.save(voucher);
        }

        // Ghi lịch sử thanh toán nếu đơn đã thanh toán ngay (POS)
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

        sendOrderConfirmationAfterCommit(hoaDon);

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

    /** Áp dụng / kiểm tra mã giảm giá (dùng cho cả client và POS). */
    @PostMapping("/apply-voucher")
    public ResponseEntity<?> applyVoucher(@RequestBody Map<String, Object> body) {
        String ma = (String) body.get("maGiamGia");
        BigDecimal tamTinh = body.get("tongTien") != null
                ? new BigDecimal(body.get("tongTien").toString()) : BigDecimal.ZERO;
        if (ma == null || ma.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Vui lòng nhập mã giảm giá"));
        }
        var vr = validateVoucher(ma.trim(), tamTinh);
        if (!vr.valid) {
            return ResponseEntity.ok(Map.of("valid", false, "message", vr.message));
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("valid", true);
        res.put("maGiamGia", vr.giamGia.getMaGiamGia());
        res.put("tenGiamGia", vr.giamGia.getTenGiamGia());
        res.put("giamGia", vr.discount);
        res.put("message", "Áp dụng mã thành công");
        return ResponseEntity.ok(res);
    }

    /** Staff-only manual confirmation for sandbox/demo payment flows. */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thieu thong tin xac nhan thanh toan"));
        }
        Integer orderId = toInt(body.get("orderId"));
        String maHoaDon = (String) body.get("maHoaDon");
        String method = (String) body.get("method");
        if (orderId == null && (maHoaDon == null || maHoaDon.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thieu orderId hoac maHoaDon"));
        }

        Optional<HoaDon> opt = orderId != null
                ? hoaDonRepo.findById(orderId)
                : (maHoaDon != null ? hoaDonRepo.findByMaHoaDon(maHoaDon) : Optional.empty());
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        }

        HoaDon hd = opt.get();
        if (hd.getTrangThai() != null && hd.getTrangThai() == STATUS_PAYMENT_FAILED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Don hang thanh toan that bai, khong the xac nhan lai"));
        }
        if (hd.getTrangThai() != null && hd.getTrangThai() == STATUS_CANCELLED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Don hang da huy, khong the xac nhan thanh toan"));
        }
        String paymentMethod = method != null && !method.isBlank() ? method : hd.getHinhThucThanhToan();

        hd.setTrangThai(STATUS_CONFIRMED);
        hd.setDaThanhToan(true);
        hd.setPhuongThucThanhToanOnline(paymentMethod);
        hoaDonRepo.save(hd);
        emailService.sendOrderStatusUpdateEmail(hd, statusLabel(STATUS_CONFIRMED),
                "Thanh toán " + paymentMethod + " đã được xác nhận. Đơn hàng đang chờ xử lý tiếp.");

        lichSuRepo.save(LichSuThanhToan.builder()
                .hoaDon(hd)
                .soTien(hd.getTongTien())
                .phuongThuc(paymentMethod)
                .maGiaoDich((paymentMethod != null ? paymentMethod : "PAY") + System.currentTimeMillis())
                .trangThai("SUCCESS")
                .noiDung("Xac nhan thanh toan sandbox - " + hd.getMaHoaDon())
                .ngayTao(LocalDateTime.now())
                .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", hd.getId());
        result.put("maHoaDon", hd.getMaHoaDon());
        result.put("trangThai", hd.getTrangThai());
        return ResponseEntity.ok(result);
    }

    /** Kết quả kiểm tra voucher. */
    private static class VoucherResult {
        boolean valid;
        String message;
        GiamGia giamGia;
        BigDecimal discount = BigDecimal.ZERO;
    }

    private VoucherResult validateVoucher(String ma, BigDecimal tamTinh) {
        return validateVoucher(ma, tamTinh, false);
    }

    private VoucherResult validateVoucher(String ma, BigDecimal tamTinh, boolean lockForUpdate) {
        VoucherResult r = new VoucherResult();
        var opt = lockForUpdate
                ? giamGiaRepo.findByMaGiamGiaIgnoreCaseForUpdate(ma)
                : giamGiaRepo.findByMaGiamGiaIgnoreCase(ma);
        if (opt.isEmpty()) {
            r.message = "Mã giảm giá không tồn tại";
            return r;
        }
        GiamGia gg = opt.get();
        if (gg.getTrangThai() != null && gg.getTrangThai() == 0) {
            r.message = "Mã giảm giá đã ngừng hoạt động";
            return r;
        }
        if (gg.getSoLuong() != null && gg.getSoLuong() <= 0) {
            r.message = "Mã giảm giá đã hết lượt sử dụng";
            return r;
        }
        var today = java.time.LocalDate.now();
        if (gg.getNgayBatDau() != null && today.isBefore(gg.getNgayBatDau())) {
            r.message = "Mã giảm giá chưa đến ngày áp dụng";
            return r;
        }
        if (gg.getNgayKetThuc() != null && today.isAfter(gg.getNgayKetThuc())) {
            r.message = "Mã giảm giá đã hết hạn";
            return r;
        }
        if (gg.getGiaTriDonToiThieu() != null && tamTinh.compareTo(gg.getGiaTriDonToiThieu()) < 0) {
            r.message = "Đơn hàng tối thiểu " + gg.getGiaTriDonToiThieu().longValue() + "đ để dùng mã này";
            return r;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (gg.getPhanTramGiam() != null && gg.getPhanTramGiam().compareTo(BigDecimal.ZERO) > 0) {
            discount = tamTinh.multiply(gg.getPhanTramGiam()).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
            if (gg.getGiamToiDa() != null && discount.compareTo(gg.getGiamToiDa()) > 0) {
                discount = gg.getGiamToiDa();
            }
        } else if (gg.getGioTriGiam() != null && gg.getGioTriGiam().compareTo(BigDecimal.ZERO) > 0) {
            discount = gg.getGioTriGiam();
        }
        if (discount.compareTo(tamTinh) > 0) discount = tamTinh;

        r.valid = true;
        r.giamGia = gg;
        r.discount = discount;
        r.message = "Áp dụng mã thành công";
        return r;
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

    @GetMapping("/my-orders")
    public ResponseEntity<?> myOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            KhachHang kh = khachHangRepo.findByEmail(username)
                    .or(() -> khachHangRepo.findBySoDienThoai(username))
                    .orElse(null);
            if (kh == null) {
                return ResponseEntity.ok(List.of());
            }
            List<HoaDon> orders = hoaDonRepo.findByKhachHangId(kh.getId());
            List<Map<String, Object>> result = new ArrayList<>();
            for (HoaDon hd : orders) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", hd.getId());
                map.put("maHoaDon", hd.getMaHoaDon());
                map.put("tongTien", hd.getTongTien());
                map.put("soSanPham", hoaDonCtRepo.countByHoaDonId(hd.getId()));
                map.put("trangThai", hd.getTrangThai());
                map.put("hinhThucThanhToan", hd.getHinhThucThanhToan());
                map.put("ngayTao", hd.getNgayTao());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token không hợp lệ"));
        }
    }

    private static Integer firstInt(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) return null;
        for (String key : keys) {
            Integer value = toInt(map.get(key));
            if (value != null) return value;
        }
        return null;
    }

    private static String cleanString(Object obj) {
        if (obj == null) return null;
        String value = String.valueOf(obj).trim();
        return value.isEmpty() ? null : value;
    }

    private static boolean isOrderableVariant(VayChiTiet variant) {
        return variant != null
                && variant.getVay() != null
                && variant.getMauSac() != null
                && variant.getKichThuoc() != null
                && (variant.getTrangThai() == null || variant.getTrangThai() == 1)
                && (variant.getVay().getTrangThai() == null || variant.getVay().getTrangThai() == 1);
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
                || "Nh\u00E2n vi\u00EAn".equalsIgnoreCase(role);
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

    private static String statusLabel(byte status) {
        return switch (status) {
            case 0 -> "Chờ xử lý";
            case 1 -> "Đã xác nhận";
            case 2 -> "Đang chuẩn bị";
            case 3 -> "Đang giao hàng";
            case 4 -> "Giao hàng thành công";
            case 5 -> "Đã hủy";
            case 6 -> "Giao hàng thất bại";
            case 7 -> "Thanh toán thất bại";
            case 8 -> "Yêu cầu đổi/trả";
            case 9 -> "Đã hoàn tiền/hoàn tất";
            default -> "Cập nhật trạng thái";
        };
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

    private void restoreStock(HoaDon hoaDon) {
        orderInventoryService.restoreReservation(hoaDon);
    }
}
