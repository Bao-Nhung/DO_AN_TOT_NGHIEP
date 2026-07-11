package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.OrderInventoryService;
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
    private static final byte STATUS_RETURN_REQUESTED = 8;
    private static final byte STATUS_REFUNDED = 9;

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final VayChiTietRepository vayCtRepo;
    private final AnhRepository anhRepo;
    
    // Khai báo thêm các Repo cần thiết cho Tracking và Xác thực (Đã vá lỗi)
    private final LichSuTrackingRepository lichSuTrackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final KhachHangRepository khachHangRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OrderInventoryService orderInventoryService;

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

    @GetMapping("/{id}/audit-log")
    public ResponseEntity<?> getAuditLog(@PathVariable Integer id) {
        if (!hoaDonRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auditLogRepo.findByHoaDonIdOrderByNgayTaoDesc(id).stream()
                .map(this::auditToMap)
                .toList());
    }

@PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (hd.getTrangThai() != null && hd.getTrangThai() == STATUS_PAYMENT_FAILED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Don hang thanh toan that bai, khong the xu ly tiep"));
            }
            if (hd.getTrangThai() != null && hd.getTrangThai() == 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng đã bị hủy, không thể thay đổi trạng thái"));
            }
            Byte changedStatus = null;
            String statusEmailDesc = null;
            if (body.get("trangThai") != null) {
                byte oldTrangThai = hd.getTrangThai() != null ? hd.getTrangThai() : 0;
                byte newTrangThai = ((Number) body.get("trangThai")).byteValue();
                hd.setTrangThai(newTrangThai);
                changedStatus = newTrangThai;
                
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
                } else if (newTrangThai == STATUS_PAYMENT_FAILED) {
                    trackingStatus = "payment_failed";
                    trackingDesc = "Thanh toán thất bại. Đơn hàng không được xử lý tiếp.";
                }
                
                if (newTrangThai == STATUS_RETURN_REQUESTED) {
                    trackingStatus = "return_requested";
                    trackingDesc = "Khách hàng yêu cầu đổi/trả hàng.";
                } else if (newTrangThai == STATUS_REFUNDED) {
                    trackingStatus = "refunded";
                    trackingDesc = "Đã xử lý đổi/trả và hoàn tiền.";
                }

                if (shouldRestoreStock(oldTrangThai, newTrangThai)) {
                    restoreStock(hd);
                }
                statusEmailDesc = trackingDesc;

                hd.setTrangThaiTracking(trackingStatus);
                
                LichSuTracking tracking = LichSuTracking.builder()
                        .hoaDon(hd)
                        .trangThai(trackingStatus)
                        .moTa(trackingDesc)
                        .ngayCapNhat(LocalDateTime.now()) // Lưu đúng thời gian bấm nút
                        .build();
                lichSuTrackingRepo.save(tracking);
                saveAuditLog(hd, "CAP_NHAT_TRANG_THAI", oldTrangThai, newTrangThai, authHeader,
                        body.get("ghiChu") != null ? String.valueOf(body.get("ghiChu")) : trackingDesc);
                // =============================================================
            }
            if (body.get("ghiChu") != null) {
                hd.setGhiChu((String) body.get("ghiChu"));
            }
            if (body.get("daThanhToan") != null) {
                hd.setDaThanhToan(Boolean.parseBoolean(String.valueOf(body.get("daThanhToan"))));
            }
            HoaDon saved = hoaDonRepo.save(hd);
            if (changedStatus != null) {
                sendStatusEmail(saved, changedStatus, statusEmailDesc);
            }
            return ResponseEntity.ok(toMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/khach-hang")
    public ResponseEntity<?> updateCustomerInfo(@PathVariable Integer id,
                                                @RequestBody Map<String, Object> body,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (body.containsKey("tenKhachHang")) {
                hd.setTenKhachHang((String) body.get("tenKhachHang"));
            }
            if (body.containsKey("soDienThoai")) {
                hd.setSoDienThoai((String) body.get("soDienThoai"));
            }
            saveAuditLog(hd, "CAP_NHAT_KHACH_HANG", hd.getTrangThai(), hd.getTrangThai(), authHeader,
                    "Cap nhat thong tin khach hang tren don.");
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
                byte oldTrangThai = hd.getTrangThai();
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
                restoreStock(hd);
                saveAuditLog(hd, "HUY_DON", oldTrangThai, STATUS_CANCELLED, authHeader, ghiChu);
                 
                HoaDon saved = hoaDonRepo.save(hd);
                emailService.sendOrderCancellationEmail(saved, ghiChu);
                return ResponseEntity.ok(toMap(saved));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ được huỷ khi đơn hàng đang chờ xử lý"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel-guest")
    public ResponseEntity<?> cancelOrderGuest(@PathVariable Integer id,
                                              @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            String maHoaDon = (String) body.get("maHoaDon");
            String soDienThoai = (String) body.get("soDienThoai");
            
            String phoneClean = soDienThoai != null ? soDienThoai.trim() : "";
            String orderPhoneClean = hd.getSoDienThoai() != null ? hd.getSoDienThoai().trim() : "";
            String codeClean = maHoaDon != null ? maHoaDon.trim() : "";
            String orderCodeClean = hd.getMaHoaDon() != null ? hd.getMaHoaDon().trim() : "";

            // Xác thực xem đúng mã đơn hàng và số điện thoại của hóa đơn này không
            if (codeClean.isEmpty() || !codeClean.equalsIgnoreCase(orderCodeClean) ||
                !phoneClean.equals(orderPhoneClean)) {
                return ResponseEntity.status(403).body(Map.of("error", "Thông tin xác thực đơn hàng không chính xác. Vui lòng nhập đúng Mã đơn hàng và Số điện thoại."));
            }
            
            if (hd.getTrangThai() == 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng này đã được huỷ trước đó."));
            }
            
            if (hd.getTrangThai() != 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng đã được xác nhận hoặc đang vận chuyển, không thể huỷ ở thời điểm này."));
            }
            
            hd.setTrangThai((byte) 5); // Trạng thái 5 = Đã Hủy
            
            String ghiChu = body.get("ghiChu") != null ? (String) body.get("ghiChu") : "Khách hàng hủy qua tra cứu đơn";
            hd.setGhiChu(ghiChu);
            hd.setTrangThaiTracking("cancelled");
            
            LichSuTracking tracking = LichSuTracking.builder()
                    .hoaDon(hd)
                    .trangThai("cancelled")
                    .moTa(ghiChu)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            lichSuTrackingRepo.save(tracking);
            restoreStock(hd);
            saveAuditLogManual(hd, "HUY_DON", (byte) 0, STATUS_CANCELLED,
                    "Khách vãng lai", "Guest", ghiChu);

            HoaDon saved = hoaDonRepo.save(hd);
            emailService.sendOrderCancellationEmail(saved, ghiChu);
            return ResponseEntity.ok(toMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/return-request")
    public ResponseEntity<?> requestReturn(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> body,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id).map(hd -> {
            ResponseEntity<?> authError = authorizeOrderAccess(hd, authHeader);
            if (authError != null) return authError;
            if (hd.getTrangThai() == null || hd.getTrangThai() != 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ đơn hàng đã hoàn thành mới được yêu cầu đổi/trả"));
            }

            byte oldTrangThai = hd.getTrangThai();
            String reason = body.get("lyDo") != null ? String.valueOf(body.get("lyDo")) : "Khách hàng yêu cầu đổi/trả hàng";
            hd.setTrangThai(STATUS_RETURN_REQUESTED);
            hd.setTrangThaiTracking("return_requested");
            hd.setGhiChu(appendNote(hd.getGhiChu(), reason));

            LichSuTracking tracking = LichSuTracking.builder()
                    .hoaDon(hd)
                    .trangThai("return_requested")
                    .moTa(reason)
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            lichSuTrackingRepo.save(tracking);
            saveAuditLog(hd, "YEU_CAU_DOI_TRA", oldTrangThai, STATUS_RETURN_REQUESTED, authHeader, reason);

            HoaDon saved = hoaDonRepo.save(hd);
            return ResponseEntity.ok(toDetailMap(saved));
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
            if (maHoaDon == null || maHoaDon.isBlank() || soDienThoai == null || soDienThoai.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập cả mã đơn hàng và số điện thoại"
                ));
            }
            Optional<HoaDon> hoaDon = hoaDonRepo.findOrderByCodeAndPhone(maHoaDon.trim(), soDienThoai.trim());

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
    public ResponseEntity<?> getOrderTracking(@PathVariable Integer hoaDonId,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Optional<HoaDon> hoaDon = hoaDonRepo.findById(hoaDonId);
            if (hoaDon.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
            }

            HoaDon order = hoaDon.get();
            ResponseEntity<?> authError = authorizeOrderAccess(order, authHeader);
            if (authError != null) return authError;

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
            emailService.sendOrderStatusUpdateEmail(order, trackingLabel(newStatus), moTa);

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
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : hd.getEmailKhachHang());
        map.put("soSanPham", soSanPham);
        
        map.put("tongTien", hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
        map.put("phiVanChuyen", hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO);
        map.put("giamGiaVoucher", hd.getGiamGiaVoucher() != null ? hd.getGiamGiaVoucher() : BigDecimal.ZERO);
        
        map.put("nhanVien", hd.getNhanVien() != null ? hd.getNhanVien().getHoVaTen() : null);
        map.put("nhanVienId", hd.getNhanVien() != null ? hd.getNhanVien().getId() : null);
        map.put("nguoiTaoDon", hd.getNhanVien() != null ? hd.getNhanVien().getHoVaTen() : "Khách hàng tự đặt");
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
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : hd.getEmailKhachHang());
        
        List<HoaDonChiTiet> chiTiets = hoaDonCtRepo.findByHoaDonId(hd.getId());
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonChiTiet ct : chiTiets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ct.getId());
            if (ct.getVayChiTiet() != null) {
                item.put("variantId", ct.getVayChiTiet().getId());
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
        map.put("auditLogs", auditLogRepo.findByHoaDonIdOrderByNgayTaoDesc(hd.getId()).stream()
                .map(this::auditToMap)
                .toList());
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

    private Map<String, Object> auditToMap(HoaDonAuditLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("hanhDong", log.getHanhDong());
        map.put("trangThaiCu", log.getTrangThaiCu());
        map.put("trangThaiMoi", log.getTrangThaiMoi());
        map.put("nguoiThucHien", log.getNguoiThucHien());
        map.put("vaiTro", log.getVaiTro());
        map.put("ghiChu", log.getGhiChu());
        map.put("ngayTao", log.getNgayTao());
        return map;
    }

    private void sendStatusEmail(HoaDon hd, byte status, String description) {
        String safeDescription = description != null && !description.isBlank()
                ? description
                : statusLabel(status);
        if (status == 4) {
            emailService.sendDeliveryConfirmationEmail(hd);
            return;
        }
        if (status == STATUS_CANCELLED) {
            emailService.sendOrderCancellationEmail(hd, safeDescription);
            return;
        }
        emailService.sendOrderStatusUpdateEmail(hd, statusLabel(status), safeDescription);
    }

    private String statusLabel(byte status) {
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

    private String trackingLabel(String status) {
        if (status == null || status.isBlank()) return "Cập nhật trạng thái";
        return switch (status) {
            case "pending" -> "Chờ xử lý";
            case "confirmed" -> "Đã xác nhận";
            case "processing" -> "Đang chuẩn bị";
            case "shipped" -> "Đang giao hàng";
            case "delivered" -> "Giao hàng thành công";
            case "cancelled" -> "Đã hủy";
            case "failed" -> "Giao hàng thất bại";
            case "payment_failed" -> "Thanh toán thất bại";
            case "return_requested" -> "Yêu cầu đổi/trả";
            case "refunded" -> "Đã hoàn tiền/hoàn tất";
            default -> status;
        };
    }

    private boolean shouldRestoreStock(byte oldStatus, byte newStatus) {
        if (oldStatus == STATUS_CANCELLED || oldStatus == 6 || oldStatus == STATUS_PAYMENT_FAILED || oldStatus == STATUS_REFUNDED) {
            return false;
        }
        return newStatus == STATUS_CANCELLED || newStatus == 6 || newStatus == STATUS_PAYMENT_FAILED || newStatus == STATUS_REFUNDED;
    }

    private void restoreStock(HoaDon hd) {
        orderInventoryService.restoreReservation(hd);
    }

    private String appendNote(String current, String note) {
        if (note == null || note.isBlank()) return current;
        if (current == null || current.isBlank()) return note;
        return current + " | " + note;
    }

    private void saveAuditLog(HoaDon hd, String action, Byte oldStatus, Byte newStatus, String authHeader, String note) {
        Actor actor = actorFromAuth(authHeader);
        saveAuditLogManual(hd, action, oldStatus, newStatus, actor.name(), actor.role(), note);
    }

    private void saveAuditLogManual(HoaDon hd, String action, Byte oldStatus, Byte newStatus,
                                    String actorName, String actorRole, String note) {
        auditLogRepo.save(HoaDonAuditLog.builder()
                .hoaDon(hd)
                .hanhDong(action)
                .trangThaiCu(oldStatus)
                .trangThaiMoi(newStatus)
                .nguoiThucHien(actorName)
                .vaiTro(actorRole)
                .ghiChu(note)
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private Actor actorFromAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Actor("System", "System");
        }
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return new Actor("System", "System");
            var claims = jwtUtil.extractClaims(token);
            String role = claims.get("role", String.class);
            String username = jwtUtil.extractUsername(token);
            return new Actor(username != null ? username : "System", role != null ? role : "System");
        } catch (Exception e) {
            return new Actor("System", "System");
        }
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
            if ("KhachHang".equalsIgnoreCase(role)
                    && hd.getKhachHang() != null
                    && Objects.equals(hd.getKhachHang().getId(), userId)) {
                return null;
            }
            return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen xem don hang nay"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
        }
    }

    private static boolean isStaffRole(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "NhanVien".equalsIgnoreCase(role)
                || "Nh\u00E2n vi\u00EAn".equalsIgnoreCase(role);
    }

    private record Actor(String name, String role) {}

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }
}
