package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {
    private static final byte STATUS_CANCELLED = 5;
    private static final byte STATUS_PAYMENT_FAILED = 7;

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final AnhRepository anhRepo;
    
    // Khai báo thêm các Repo cần thiết cho Tracking và Xác thực (Đã vá lỗi)
    private final LichSuTrackingRepository lichSuTrackingRepo;
    private final KhachHangRepository khachHangRepo;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return hoaDonRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return hoaDonRepo.findById(id)
                .map(hd -> ResponseEntity.ok(toDetailMap(hd)))
                .orElse(ResponseEntity.notFound().build());
    }

@PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (hd.getTrangThai() != null && hd.getTrangThai() == STATUS_PAYMENT_FAILED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Don hang thanh toan that bai, khong the xu ly tiep"));
            }
            if (hd.getTrangThai() != null && hd.getTrangThai() == 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng đã bị hủy, không thể thay đổi trạng thái"));
            }
            if (body.get("trangThai") != null) {
                byte newTrangThai = ((Number) body.get("trangThai")).byteValue();
                hd.setTrangThai(newTrangThai);
                
                // ===== ĐỒNG BỘ CHUẨN 7 TRẠNG THÁI (0 đến 6) VỚI FRONTEND =====
                String trackingStatus = "pending";
                String trackingDesc = "Đơn hàng đang chờ xử lý.";
                
                if (newTrangThai == 1) { 
                    trackingStatus = "confirmed"; 
                    trackingDesc = "Đơn hàng đã được xác nhận."; 
                } else if (newTrangThai == 2) { 
                    trackingStatus = "processing"; 
                    trackingDesc = "Đơn hàng đang được chuẩn bị và đóng gói."; 
                } else if (newTrangThai == 3) { 
                    trackingStatus = "shipped"; 
                    trackingDesc = "Đơn hàng đã được bàn giao cho đơn vị vận chuyển."; 
                } else if (newTrangThai == 4) { 
                    trackingStatus = "delivered"; 
                    trackingDesc = "Giao hàng thành công đến tay người nhận."; 
                    hd.setNgayGiaoHangThucTe(LocalDateTime.now()); 
                } else if (newTrangThai == 5) { 
                    trackingStatus = "cancelled"; 
                    trackingDesc = "Đơn hàng đã bị hủy."; 
                } else if (newTrangThai == 6) { 
                    trackingStatus = "failed"; 
                    trackingDesc = "Giao hàng thất bại."; 
                }
                
                hd.setTrangThaiTracking(trackingStatus);
                
                LichSuTracking tracking = LichSuTracking.builder()
                        .hoaDon(hd)
                        .trangThai(trackingStatus)
                        .moTa(trackingDesc)
                        .ngayCapNhat(LocalDateTime.now()) // Lưu đúng thời gian bấm nút
                        .build();
                lichSuTrackingRepo.save(tracking);
                // =============================================================
            }
            if (body.get("ghiChu") != null) {
                hd.setGhiChu((String) body.get("ghiChu"));
            }
            if (body.get("daThanhToan") != null) {
                hd.setDaThanhToan(Boolean.parseBoolean(String.valueOf(body.get("daThanhToan"))));
            }
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(toMap(hd));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/khach-hang")
    public ResponseEntity<?> updateCustomerInfo(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (body.containsKey("tenKhachHang")) {
                hd.setTenKhachHang((String) body.get("tenKhachHang"));
            }
            if (body.containsKey("soDienThoai")) {
                hd.setSoDienThoai((String) body.get("soDienThoai"));
            }
            HoaDon saved = hoaDonRepo.save(hd);
            return ResponseEntity.ok(toDetailMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id,
                                         @RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id).map(hd -> {
            ResponseEntity<?> authError = authorizeCancel(hd, authHeader);
            if (authError != null) return authError;
            if (hd.getTrangThai() == 0) {
                hd.setTrangThai((byte) 5); // Trạng thái 5 = Đã Hủy
                
                String ghiChu = body.get("ghiChu") != null ? (String) body.get("ghiChu") : "Khách hàng yêu cầu hủy";
                hd.setGhiChu(ghiChu);
                hd.setTrangThaiTracking("cancelled");
                
                LichSuTracking tracking = LichSuTracking.builder()
                        .hoaDon(hd)
                        .trangThai("cancelled")
                        .moTa(ghiChu)
                        .ngayCapNhat(LocalDateTime.now())
                        .build();
                lichSuTrackingRepo.save(tracking);
                
                hoaDonRepo.save(hd);
                return ResponseEntity.ok(toMap(hd));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ được huỷ khi đơn hàng đang chờ xử lý"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ========================================================
    // ===== TRACKING ENDPOINTS (TÍCH HỢP TỪ CODE ĐỒNG ĐỘI) =====
    // ========================================================

    @GetMapping("/search")
    public ResponseEntity<?> searchOrder(
            @RequestParam String maHoaDon,
            @RequestParam(required = false) String soDienThoai) {
        try {
            Optional<HoaDon> hoaDon;
            if (soDienThoai != null && !soDienThoai.isEmpty()) {
                hoaDon = hoaDonRepo.findOrderByCodeAndPhone(maHoaDon, soDienThoai);
            } else {
                hoaDon = hoaDonRepo.findOrderByCode(maHoaDon);
            }

            if (hoaDon.isPresent()) {
                Map<String, Object> result = toDetailMap(hoaDon.get());
                List<LichSuTracking> trackingHistory = lichSuTrackingRepo
                        .findByHoaDonIdOrderByLatest(hoaDon.get().getId());
                result.put("trackingHistory", trackingHistory.stream()
                        .map(this::trackingToMap)
                        .toList());
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy đơn hàng"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tìm kiếm: " + e.getMessage()
            ));
        }
    }

    // ĐÃ VÁ LỖI: Sử dụng JwtUtil và KhachHangRepository thay vì Authentication mặc định bị lỗi
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            KhachHang kh = khachHangRepo.findByEmail(username)
                    .or(() -> khachHangRepo.findBySoDienThoai(username))
                    .orElse(null);

            if (kh == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }

            List<HoaDon> orders = hoaDonRepo.findByCustomerIdOrderByLatest(kh.getId());
            List<Map<String, Object>> result = orders.stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(Map.of("success", true, "data", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/customer/{khachHangId}")
    public ResponseEntity<?> getOrdersByCustomerId(@PathVariable Integer khachHangId) {
        try {
            List<HoaDon> orders = hoaDonRepo.findByCustomerIdOrderByLatest(khachHangId);
            List<Map<String, Object>> result = orders.stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{hoaDonId}/tracking")
    public ResponseEntity<?> getOrderTracking(@PathVariable Integer hoaDonId) {
        try {
            Optional<HoaDon> hoaDon = hoaDonRepo.findById(hoaDonId);
            if (hoaDon.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
            }

            HoaDon order = hoaDon.get();
            List<LichSuTracking> trackingHistory = lichSuTrackingRepo
                    .findByHoaDonIdOrderByLatest(hoaDonId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", order.getId());
            result.put("maHoaDon", order.getMaHoaDon());
            result.put("trangThaiTracking", order.getTrangThaiTracking());
            result.put("ngayGiaoHangDuKien", order.getNgayGiaoHangDuKien());
            result.put("ngayGiaoHangThucTe", order.getNgayGiaoHangThucTe());
            result.put("diaChiGiaoHang", order.getDiaChiGiaoHang());
            result.put("khachHang", order.getKhachHang() != null ? Map.of(
                    "hoVaTen", order.getKhachHang().getHoVaTen(),
                    "soDienThoai", order.getKhachHang().getSoDienThoai(),
                    "email", order.getKhachHang().getEmail()
            ) : null);
            result.put("trackingHistory", trackingHistory.stream()
                    .map(this::trackingToMap)
                    .toList());
            result.put("ngayTao", order.getNgayTao());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{hoaDonId}/tracking/update")
    public ResponseEntity<?> updateOrderTracking(@PathVariable Integer hoaDonId, @RequestBody Map<String, Object> body) {
        try {
            Optional<HoaDon> hoaDon = hoaDonRepo.findById(hoaDonId);
            if (hoaDon.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
            }

            HoaDon order = hoaDon.get();
            String newStatus = (String) body.get("trangThaiTracking");
            String moTa = (String) body.getOrDefault("moTa", "");

            order.setTrangThaiTracking(newStatus);

            if ("delivered".equalsIgnoreCase(newStatus)) {
                order.setNgayGiaoHangThucTe(LocalDateTime.now());
            }

            hoaDonRepo.save(order);

            LichSuTracking tracking = LichSuTracking.builder()
                    .hoaDon(order)
                    .trangThai(newStatus)
                    .moTa(moTa)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            lichSuTrackingRepo.save(tracking);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "data", toDetailMap(order)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/search-by-phone")
    public ResponseEntity<?> searchOrderByPhone(@RequestParam String soDienThoai) {
        try {
            List<HoaDon> orders = hoaDonRepo.findOrdersByPhoneNumber(soDienThoai);
            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng nào"));
            }
            List<Map<String, Object>> result = orders.stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // ========================================================
    // ===== HELPER METHODS (ĐÃ TÍCH HỢP ĐẦY ĐỦ DỮ LIỆU) =======
    // ========================================================

    private Map<String, Object> toMap(HoaDon hd) {
        long soSanPham = hoaDonCtRepo.countByHoaDonId(hd.getId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hd.getId());
        map.put("maHoaDon", hd.getMaHoaDon());
        
        String tenKH = hd.getKhachHang() != null ? hd.getKhachHang().getHoVaTen() : hd.getTenKhachHang();
        String sdt = hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : hd.getSoDienThoai();
        map.put("khachHang", tenKH != null ? tenKH : "Khách lẻ");
        map.put("soDienThoai", sdt);
        map.put("tenKhachHang", hd.getTenKhachHang());
        map.put("soSanPham", soSanPham);
        
        map.put("tongTien", hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
        map.put("phiVanChuyen", hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO);
        map.put("giamGiaKhuyenMai", hd.getGiamGiaKhuyenMai() != null ? hd.getGiamGiaKhuyenMai() : BigDecimal.ZERO);
        
        map.put("nhanVien", hd.getNhanVien() != null ? hd.getNhanVien().getHoVaTen() : null);
        map.put("nhanVienId", hd.getNhanVien() != null ? hd.getNhanVien().getId() : null);
        map.put("nguoiTaoDon", hd.getNhanVien() != null ? hd.getNhanVien().getHoVaTen() : "Khach hang tu dat");
        map.put("khuyenMai", hd.getKhuyenMai() != null ? hd.getKhuyenMai().getTenKhuyenMai() : null);
        map.put("giamGia", hd.getGiamGia() != null ? hd.getGiamGia().getTenGiamGia() : null);
        map.put("hinhThucThanhToan", hd.getHinhThucThanhToan());
        map.put("phuongThucThanhToanOnline", hd.getPhuongThucThanhToanOnline());
        map.put("daThanhToan", Boolean.TRUE.equals(hd.getDaThanhToan()));
        
        // Trạng thái chung và trạng thái Tracking mới
        map.put("trangThai", hd.getTrangThai()); 
        map.put("trangThaiTracking", hd.getTrangThaiTracking()); 
        
        map.put("diaChiGiaoHang", hd.getDiaChiGiaoHang());
        map.put("hinhThucNhanHang", hd.getHinhThucNhanHang()); 
        map.put("ghiChu", hd.getGhiChu());
        map.put("ngayTao", hd.getNgayTao());
        map.put("ngayGiaoHangDuKien", hd.getNgayGiaoHangDuKien()); 
        
        return map;
    }

    private Map<String, Object> toDetailMap(HoaDon hd) {
        Map<String, Object> map = toMap(hd);
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : null);
        
        List<HoaDonChiTiet> chiTiets = hoaDonCtRepo.findByHoaDonId(hd.getId());
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonChiTiet ct : chiTiets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ct.getId());
            if (ct.getVayChiTiet() != null) {
                item.put("tenVay", ct.getVayChiTiet().getVay() != null 
                        ? ct.getVayChiTiet().getVay().getTenVay() : null);
                item.put("maSanPham", ct.getVayChiTiet().getVay() != null 
                        ? ct.getVayChiTiet().getVay().getMaVay() : null);
                item.put("mauSac", ct.getVayChiTiet().getMauSac() != null 
                        ? ct.getVayChiTiet().getMauSac().getTenMauSac() : null);
                item.put("maHex", ct.getVayChiTiet().getMauSac() != null 
                        ? ct.getVayChiTiet().getMauSac().getMaHex() : null);
                item.put("kichThuoc", ct.getVayChiTiet().getKichThuoc() != null 
                        ? ct.getVayChiTiet().getKichThuoc().getTenKichThuoc() : null);
                
                if (ct.getVayChiTiet().getVay() != null) {
                    List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(
                            ct.getVayChiTiet().getVay().getId(), (byte) 1);
                    item.put("anhUrl", !anhs.isEmpty() ? anhs.get(0).getAnhUrl() : null);
                }
            }
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
            item.put("phanTramGiam", ct.getPhanTramGiam());
            items.add(item);
        }
        map.put("chiTiets", items);
        return map;
    }

    private Map<String, Object> trackingToMap(LichSuTracking tracking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tracking.getId());
        map.put("trangThai", tracking.getTrangThai());
        map.put("moTa", tracking.getMoTa());
        map.put("ngayCapNhat", tracking.getNgayCapNhat());
        return map;
    }

    private ResponseEntity<?> authorizeCancel(HoaDon hd, String authHeader) {
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
            if ("KhachHang".equalsIgnoreCase(role)
                    && hd.getKhachHang() != null
                    && Objects.equals(hd.getKhachHang().getId(), userId)) {
                return null;
            }
            return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen huy don hang nay"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
        }
    }

    private static boolean isStaffRole(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "NhanVien".equalsIgnoreCase(role)
                || "Nh\u00E2n vi\u00EAn".equalsIgnoreCase(role);
    }

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }
}
