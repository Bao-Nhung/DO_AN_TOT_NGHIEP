package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.config.VNPayConfig;
import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final VayChiTietRepository vayCtRepo;
    private final KhachHangRepository khachHangRepo;
    private final LichSuThanhToanRepository lichSuRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final NhanVienRepository nhanVienRepo;
    private final VNPayConfig vnPayConfig;
    private final JwtUtil jwtUtil;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String hoTen = (String) body.get("hoTen");
        if (hoTen == null) {
            hoTen = (String) body.get("tenKhachHang");
        }
        String soDienThoai = (String) body.get("soDienThoai");
        String diaChi = (String) body.get("diaChi");
        String ghiChu = (String) body.get("ghiChu");
        String hinhThuc = (String) body.get("hinhThucThanhToan");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
        }

        KhachHang kh = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                kh = khachHangRepo.findByEmail(username)
                        .or(() -> khachHangRepo.findBySoDienThoai(username))
                        .orElse(null);
            } catch (Exception ignored) {}
        }

        String maHoaDon = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                          + String.format("%04d", new Random().nextInt(10000));

        BigDecimal tamTinh = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (Map<String, Object> item : items) {
            // === THÊM 1 DÒNG ĐỂ ĐỌC ĐƯỢC CẢ "productId" HOẶC "id" TỪ FRONTEND GỬI LÊN ===
            Integer productId = toInt(item.get("productId"));
            if (productId == null) productId = toInt(item.get("id")); // Dòng này là "cứu tinh"
            
            Integer qty = toInt(item.get("qty"));
            if (qty == null) qty = toInt(item.get("quantity"));
            if (productId == null || qty == null || qty <= 0) continue;

            var variants = vayCtRepo.findByVayId(productId);
            if (variants.isEmpty()) continue;

            // ==========================================
            // TÌM CHÍNH XÁC BIẾN THỂ (KÍCH THƯỚC & MÀU SẮC)
            // ==========================================
            VayChiTiet variant = variants.get(0); // Lấy mặc định nếu không khớp
            String size = (String) item.get("size");
            String color = (String) item.get("color");

            if (size != null || color != null) {
                for (VayChiTiet v : variants) {
                    boolean matchSize = size == null || (v.getKichThuoc() != null && size.equalsIgnoreCase(v.getKichThuoc().getTenKichThuoc()));
                    boolean matchColor = color == null || (v.getMauSac() != null && color.equalsIgnoreCase(v.getMauSac().getTenMauSac()));
                    
                    if (matchSize && matchColor) {
                        variant = v;
                        break;
                    }
                }
            }

            // ==========================================
            // KIỂM TRA VÀ TRỪ TỒN KHO THỰC TẾ
            // ==========================================
            int soLuongKho = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            if (soLuongKho < qty) {
                String tenSp = variant.getVay() != null ? variant.getVay().getTenVay() : "Sản phẩm";
                String thongTinBT = (color != null ? " - Màu " + color : "") + (size != null ? " - Size " + size : "");
                return ResponseEntity.badRequest().body(Map.of("error", 
                    tenSp + thongTinBT + " không đủ số lượng. Chỉ còn " + soLuongKho + " sản phẩm."));
            }
            
            // Tiến hành trừ kho ngay khi đặt hàng
            variant.setSoLuong(soLuongKho - qty);
            vayCtRepo.save(variant);
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
        BigDecimal phiVanChuyen = BigDecimal.ZERO;
        if (body.get("phiVanChuyen") != null) {
            phiVanChuyen = new BigDecimal(body.get("phiVanChuyen").toString());
        }

        // Áp dụng voucher (mã giảm giá) nếu có
        GiamGia voucher = null;
        BigDecimal giamGia = BigDecimal.ZERO;
        String maGiamGia = (String) body.get("maGiamGia");
        if (maGiamGia != null && !maGiamGia.isBlank()) {
            var vr = validateVoucher(maGiamGia.trim(), tamTinh);
            if (vr.valid) {
                voucher = vr.giamGia;
                giamGia = vr.discount;
            }
        }

        // CẬP NHẬT TÍNH TỔNG TIỀN: Tiền hàng + Phí Ship - Giảm giá
        BigDecimal tongTien = tamTinh.add(phiVanChuyen).subtract(giamGia);
        if (tongTien.compareTo(BigDecimal.ZERO) < 0) tongTien = BigDecimal.ZERO;

        // Trạng thái: mặc định 0 (Chờ xử lý). POS gửi trangThai=4 (Hoàn thành - cập nhật theo luồng mới).
        byte trangThai = (byte) 0;
        if (body.get("trangThai") != null) trangThai = ((Number) body.get("trangThai")).byteValue();

        // Nhân viên (đơn bán tại quầy)
        NhanVien nv = null;
        Integer nhanVienId = toInt(body.get("nhanVienId"));
        if (nhanVienId != null) {
            nv = nhanVienRepo.findById(nhanVienId).orElse(null);
        }

        // Đã thanh toán: POS = true; nếu body chỉ định thì theo body
        boolean daThanhToan = trangThai >= 3;
        if (body.get("daThanhToan") != null) {
            daThanhToan = Boolean.parseBoolean(String.valueOf(body.get("daThanhToan")));
        }

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(maHoaDon)
                .khachHang(kh)
                .nhanVien(nv)
                .giamGia(voucher)
                .tongTien(tongTien)
                .phiVanChuyen(phiVanChuyen)
                .giamGiaKhuyenMai(giamGia)
                .hinhThucNhanHang(body.get("nhanVienId") != null ? (byte) 0 : (byte) 1)
                .diaChiGiaoHang(diaChi != null ? diaChi : "")
                .hinhThucThanhToan(hinhThuc != null ? hinhThuc : "COD")
                .trangThai(trangThai)
                .daThanhToan(daThanhToan)
                .ghiChu(ghiChu)
                .ngayTao(LocalDateTime.now())
                .tenKhachHang(hoTen)
                .soDienThoai(soDienThoai)
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", hoaDon.getId());
        result.put("maHoaDon", maHoaDon);
        result.put("tamTinh", tamTinh);
        result.put("giamGia", giamGia);
        result.put("phiVanChuyen", phiVanChuyen);
        result.put("tongTien", tongTien);
        result.put("hinhThucThanhToan", hoaDon.getHinhThucThanhToan());

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

    /** Xác nhận thanh toán online (MoMo / ZaloPay / VNPay) — đơn được xác nhận ngay. */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> body) {
        Integer orderId = toInt(body.get("orderId"));
        String maHoaDon = (String) body.get("maHoaDon");
        String method = (String) body.get("method");

        Optional<HoaDon> opt = orderId != null
                ? hoaDonRepo.findById(orderId)
                : (maHoaDon != null ? hoaDonRepo.findByMaHoaDon(maHoaDon) : Optional.empty());
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        }

        HoaDon hd = opt.get();
        hd.setTrangThai((byte) 1); // Đã xác nhận
        hd.setDaThanhToan(true);
        hd.setPhuongThucThanhToanOnline(method != null ? method : hd.getHinhThucThanhToan());
        hoaDonRepo.save(hd);

        lichSuRepo.save(LichSuThanhToan.builder()
                .hoaDon(hd)
                .soTien(hd.getTongTien())
                .phuongThuc(method != null ? method : hd.getHinhThucThanhToan())
                .maGiaoDich((method != null ? method : "PAY") + System.currentTimeMillis())
                .trangThai("SUCCESS")
                .noiDung("Thanh toán online thành công - " + hd.getMaHoaDon())
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
        VoucherResult r = new VoucherResult();
        var opt = giamGiaRepo.findByMaGiamGiaIgnoreCase(ma);
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

    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createVNPayUrl(@RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {
        Integer orderId = toInt(body.get("orderId"));
        if (orderId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu orderId"));
        }

        Optional<HoaDon> opt = hoaDonRepo.findById(orderId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng không tồn tại"));
        }

        HoaDon hd = opt.get();
        long amount = hd.getTongTien().longValue();
        String orderInfo = "Thanh toan don hang " + hd.getMaHoaDon();
        String ipAddr = request.getRemoteAddr();

        if (vnPayConfig.isDemoMode()) {
            String demoReturnUrl = vnPayConfig.getReturnUrl()
                    + "?vnp_TxnRef=" + hd.getMaHoaDon()
                    + "&vnp_Amount=" + (amount * 100)
                    + "&vnp_ResponseCode=00"
                    + "&vnp_TransactionNo=DEMO" + System.currentTimeMillis()
                    + "&vnp_OrderInfo=" + java.net.URLEncoder.encode(orderInfo, java.nio.charset.StandardCharsets.UTF_8)
                    + "&vnp_SecureHash=DEMO_MODE";

            return ResponseEntity.ok(Map.of("paymentUrl", demoReturnUrl));
        }

        String paymentUrl = vnPayConfig.createPaymentUrl(amount, hd.getMaHoaDon(), orderInfo, ipAddr);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @GetMapping("/vnpay/return")
    public void vnpayReturn(@RequestParam Map<String, String> params,
                            HttpServletResponse response) throws IOException {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        boolean isDemo = "DEMO_MODE".equals(params.get("vnp_SecureHash"));
        boolean validSignature = isDemo || vnPayConfig.validateSignature(params);

        String frontendBase = "http://localhost:5173/#/payment-result";

        if (!validSignature) {
            response.sendRedirect(frontendBase + "?status=error&code=INVALID_SIGNATURE");
            return;
        }

        Optional<HoaDon> opt = hoaDonRepo.findByMaHoaDon(txnRef);
        if (opt.isEmpty()) {
            response.sendRedirect(frontendBase + "?status=error&code=ORDER_NOT_FOUND");
            return;
        }

        HoaDon hd = opt.get();
        boolean success = "00".equals(responseCode);

        if (success) {
            hd.setTrangThai((byte) 1);
            hd.setPhuongThucThanhToanOnline("VNPAY");
            hoaDonRepo.save(hd);

            BigDecimal soTien = BigDecimal.ZERO;
            if (amountStr != null) {
                soTien = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            }

            LichSuThanhToan ls = LichSuThanhToan.builder()
                    .hoaDon(hd)
                    .soTien(soTien)
                    .phuongThuc("VNPAY")
                    .maGiaoDich(transactionNo)
                    .trangThai("SUCCESS")
                    .noiDung("Thanh toán VNPay thành công - " + txnRef)
                    .ngayTao(LocalDateTime.now())
                    .build();
            lichSuRepo.save(ls);
        } else {
            // Thanh toán VNPay thất bại, không lưu là Đã Hủy (5) nữa mà đổi về Giao Thất Bại (6) hoặc giữ Chờ Xử Lý (0)
            hd.setTrangThai((byte) 6);
            hoaDonRepo.save(hd);

            LichSuThanhToan ls = LichSuThanhToan.builder()
                    .hoaDon(hd)
                    .soTien(BigDecimal.ZERO)
                    .phuongThuc("VNPAY")
                    .maGiaoDich(transactionNo)
                    .trangThai("FAILED")
                    .noiDung("Thanh toán VNPay thất bại - mã lỗi: " + responseCode)
                    .ngayTao(LocalDateTime.now())
                    .build();
            lichSuRepo.save(ls);
        }

        response.sendRedirect(frontendBase
                + "?status=" + (success ? "success" : "failed")
                + "&orderId=" + hd.getMaHoaDon()
                + "&amount=" + hd.getTongTien()
                + "&txn=" + (transactionNo != null ? transactionNo : ""));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Integer id) {
        return hoaDonRepo.findById(id)
                .map(hd -> {
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

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }
}