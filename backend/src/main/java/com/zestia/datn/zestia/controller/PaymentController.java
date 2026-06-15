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
    private final VNPayConfig vnPayConfig;
    private final JwtUtil jwtUtil;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String hoTen = (String) body.get("hoTen");
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

        BigDecimal tongTien = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Integer productId = toInt(item.get("productId"));
            Integer qty = toInt(item.get("qty"));
            if (productId == null || qty == null || qty <= 0) continue;

            var variants = vayCtRepo.findByVayId(productId);
            if (variants.isEmpty()) continue;

            VayChiTiet variant = variants.get(0);
            BigDecimal price = variant.getGiaBan();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
            tongTien = tongTien.add(lineTotal);

            BigDecimal phanTramGiam = BigDecimal.ZERO;
            if (variant.getGiaBanGoc() != null && variant.getGiaBanGoc().compareTo(variant.getGiaBan()) > 0) {
                phanTramGiam = BigDecimal.ONE.subtract(
                    variant.getGiaBan().divide(variant.getGiaBanGoc(), 4, java.math.RoundingMode.HALF_UP)
                ).multiply(BigDecimal.valueOf(100));
            }

            chiTietList.add(HoaDonChiTiet.builder()
                    .vayChiTiet(variant)
                    .soLuong(qty)
                    .donGia(price)
                    .phanTramGiam(phanTramGiam)
                    .build());
        }

        if (chiTietList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có sản phẩm hợp lệ"));
        }

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(maHoaDon)
                .khachHang(kh)
                .tongTien(tongTien)
                .phiVanChuyen(BigDecimal.ZERO)
                .giamGiaKhuyenMai(BigDecimal.ZERO)
                .hinhThucNhanHang((byte) 1)
                .diaChiGiaoHang(diaChi != null ? diaChi : "")
                .hinhThucThanhToan(hinhThuc != null ? hinhThuc : "COD")
                .trangThai((byte) 0)
                .ghiChu(ghiChu)
                .ngayTao(LocalDateTime.now())
                .build();

        hoaDon = hoaDonRepo.save(hoaDon);

        for (HoaDonChiTiet ct : chiTietList) {
            ct.setHoaDon(hoaDon);
        }
        hoaDonCtRepo.saveAll(chiTietList);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", hoaDon.getId());
        result.put("maHoaDon", maHoaDon);
        result.put("tongTien", tongTien);
        result.put("hinhThucThanhToan", hoaDon.getHinhThucThanhToan());

        return ResponseEntity.ok(result);
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
            hd.setTrangThai((byte) 5);
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
