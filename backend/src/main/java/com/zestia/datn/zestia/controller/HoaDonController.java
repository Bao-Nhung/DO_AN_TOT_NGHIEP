package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.YeuCauDoiTraRepository;
import com.zestia.datn.zestia.service.EmailService;
import com.zestia.datn.zestia.service.OrderInventoryService;
import com.zestia.datn.zestia.service.OrderStatusService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import com.zestia.datn.zestia.service.ReturnExchangeService;
import com.zestia.datn.zestia.service.CurrentCustomerService;
import com.zestia.datn.zestia.service.LoyaltyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
@Slf4j
public class HoaDonController {
    private static final byte STATUS_CANCELLED = 5;
    private static final byte STATUS_PAYMENT_FAILED = 7;
    private static final int CANCEL_OTP_TTL_MINUTES = 5;
    private static final int CANCEL_OTP_RESEND_SECONDS = 60;
    private static final int CANCEL_OTP_MAX_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final YeuCauDoiTraRepository returnRequestRepo;
    private final AnhRepository anhRepo;
    
    private final LichSuTrackingRepository lichSuTrackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final KhachHangRepository khachHangRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OrderInventoryService orderInventoryService;
    private final PasswordEncoder passwordEncoder;
    private final OrderStatusService orderStatusService;
    private final RequestRateLimiter rateLimiter;
    private final CurrentCustomerService currentCustomerService;
    private final LoyaltyService loyaltyService;

    @GetMapping("/paged")
    public Map<String, Object> getPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) Byte status,
                                       @RequestParam(required = false) Byte orderType) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String keyword = q == null || q.isBlank() ? null : q.trim();
        var result = hoaDonRepo.findAdminPage(
                keyword,
                status,
                orderType,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "ngayTao", "id"))
        );
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (int state = 0; state <= 7; state++) statusCounts.put(String.valueOf(state), 0L);
        for (Object[] row : hoaDonRepo.countAdminOrdersByStatus(keyword, orderType)) {
            if (row[0] != null) statusCounts.put(String.valueOf(((Number) row[0]).intValue()), ((Number) row[1]).longValue());
        }
        long allStatuses = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", toMaps(result.getContent()));
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("allStatusesTotal", allStatuses);
        response.put("statusCounts", statusCounts);
        return response;
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getById(@PathVariable Integer id,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findById(id).map(hd -> {
            ResponseEntity<?> authError = authorizeOrderAccess(hd, authHeader);
            return authError != null ? authError : ResponseEntity.ok(toDetailMap(hd));
        }).orElse(ResponseEntity.notFound().build());
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
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu trạng thái đơn hàng không hợp lệ"));
        }
        Integer parsedStatus = toInt(body.get("trangThai"));
        if (parsedStatus == null || parsedStatus < 0 || parsedStatus > 7) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái đơn hàng không hợp lệ"));
        }
        if (parsedStatus == STATUS_PAYMENT_FAILED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái thanh toán thất bại chỉ do cổng thanh toán cập nhật"));
        }
        Actor actor = actorFromAuth(authHeader);
        try {
            HoaDon saved = orderStatusService.transition(
                    id,
                    parsedStatus.byteValue(),
                    cleanText(body.get("ghiChu")),
                    actor.name(),
                    actor.role()
            );
            return ResponseEntity.ok(toMap(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id,
                                         @RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return hoaDonRepo.findByIdForUpdate(id).map(hd -> {
            ResponseEntity<?> authError = authorizeCancel(hd, authHeader);
            if (authError != null) return authError;
            if (Boolean.TRUE.equals(hd.getDaThanhToan())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Đơn đã thanh toán không thể hủy trực tiếp. Vui lòng yêu cầu trả hàng và hoàn tiền"
                ));
            }
            if (hd.getTrangThai() == 0) {
                byte oldTrangThai = hd.getTrangThai();
                hd.setTrangThai((byte) 5);
                
                String ghiChu = cleanText(body != null ? body.get("ghiChu") : null);
                if (ghiChu == null) ghiChu = "Khách hàng yêu cầu hủy";
                if (ghiChu.length() > 1000) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ghi chú hủy đơn không được vượt quá 1000 ký tự"));
                }
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

    @PostMapping("/{id}/cancel-guest/request-otp")
    @Transactional
    public ResponseEntity<?> requestCancelOrderGuestOtp(@PathVariable Integer id,
                                                        @RequestBody Map<String, Object> body,
                                                        HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("guest-cancel-otp", clientIp(request), 10, 15 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã yêu cầu quá nhiều mã OTP. Vui lòng thử lại sau"
            ));
        }
        return hoaDonRepo.findByIdForUpdate(id).map(hd -> {
            if (!matchesGuestOrder(hd, body)) {
                return ResponseEntity.status(403).body(Map.of("error", "Không thể xác minh thông tin đơn hàng"));
            }

            ResponseEntity<?> statusError = validateGuestCancellationStatus(hd);
            if (statusError != null) {
                return statusError;
            }

            String email = resolveOrderEmail(hd);
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Đơn hàng chưa có email nhận OTP. Vui lòng liên hệ cửa hàng để được hỗ trợ hủy đơn."
                ));
            }
            if (!emailService.isConfigured()) {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "Dịch vụ email chưa được cấu hình nên chưa thể gửi OTP hủy đơn"
                ));
            }

            LocalDateTime now = LocalDateTime.now();
            if (hd.getHuyDonOtpGuiLuc() != null) {
                LocalDateTime nextAllowedAt = hd.getHuyDonOtpGuiLuc().plusSeconds(CANCEL_OTP_RESEND_SECONDS);
                if (now.isBefore(nextAllowedAt)) {
                    long waitSeconds = Math.max(1, ChronoUnit.SECONDS.between(now, nextAllowedAt));
                    return ResponseEntity.status(429).body(Map.of(
                            "error", "Vui lòng chờ " + waitSeconds + " giây trước khi gửi lại OTP",
                            "retryAfterSeconds", waitSeconds
                    ));
                }
            }

            String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
            LocalDateTime expiresAt = now.plusMinutes(CANCEL_OTP_TTL_MINUTES);
            hd.setHuyDonOtpHash(passwordEncoder.encode(otp));
            hd.setHuyDonOtpHetHan(expiresAt);
            hd.setHuyDonOtpSoLanSai(0);
            hd.setHuyDonOtpGuiLuc(now);
            HoaDon saved = hoaDonRepo.save(hd);
            emailService.sendOrderCancellationOtpEmail(saved, otp, expiresAt);

            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP đã được gửi tới email " + maskEmail(email),
                    "emailMasked", maskEmail(email),
                    "expiresInSeconds", CANCEL_OTP_TTL_MINUTES * 60
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel-guest")
    @Transactional
    public ResponseEntity<?> cancelOrderGuest(@PathVariable Integer id,
                                              @RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("guest-cancel-verify", clientIp(request), 30, 15 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Bạn đã thử xác nhận quá nhiều lần. Vui lòng thử lại sau"
            ));
        }
        return hoaDonRepo.findByIdForUpdate(id).map(hd -> {
            if (!matchesGuestOrder(hd, body)) {
                return ResponseEntity.status(403).body(Map.of("error", "Không thể xác minh thông tin đơn hàng"));
            }

            ResponseEntity<?> statusError = validateGuestCancellationStatus(hd);
            if (statusError != null) {
                return statusError;
            }

            String otp = cleanText(body.get("otp"));
            if (otp == null || !otp.matches("\\d{6}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập mã OTP gồm 6 chữ số"));
            }
            if (hd.getHuyDonOtpHash() == null || hd.getHuyDonOtpHetHan() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng yêu cầu gửi mã OTP trước khi hủy đơn"));
            }
            if (LocalDateTime.now().isAfter(hd.getHuyDonOtpHetHan())) {
                clearCancellationOtp(hd);
                hoaDonRepo.save(hd);
                return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới"));
            }

            int failedAttempts = Optional.ofNullable(hd.getHuyDonOtpSoLanSai()).orElse(0);
            if (failedAttempts >= CANCEL_OTP_MAX_ATTEMPTS) {
                return ResponseEntity.status(429).body(Map.of("error", "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng yêu cầu mã mới"));
            }
            if (!passwordEncoder.matches(otp, hd.getHuyDonOtpHash())) {
                failedAttempts++;
                hd.setHuyDonOtpSoLanSai(failedAttempts);
                hoaDonRepo.save(hd);
                int remainingAttempts = Math.max(0, CANCEL_OTP_MAX_ATTEMPTS - failedAttempts);
                return ResponseEntity.status(403).body(Map.of(
                        "error", remainingAttempts > 0
                                ? "Mã OTP không chính xác. Bạn còn " + remainingAttempts + " lần thử"
                                : "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng yêu cầu mã mới",
                        "remainingAttempts", remainingAttempts
                ));
            }

            byte oldStatus = hd.getTrangThai();
            clearCancellationOtp(hd);
            hd.setTrangThai(STATUS_CANCELLED);

            String ghiChu = cleanText(body.get("ghiChu"));
            if (ghiChu == null) ghiChu = "Khách hàng hủy qua tra cứu đơn";
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
            saveAuditLogManual(hd, "HUY_DON", oldStatus, STATUS_CANCELLED,
                    "Khách vãng lai", "Guest", ghiChu);

            HoaDon saved = hoaDonRepo.save(hd);
            emailService.sendOrderCancellationEmail(saved, ghiChu);
            return ResponseEntity.ok(toMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchOrder(
            @RequestParam String maHoaDon,
            @RequestParam(required = false) String soDienThoai,
            HttpServletRequest request) {
        if (!rateLimiter.tryAcquire("guest-order-search", clientIp(request), 30, 5 * 60)) {
            return ResponseEntity.status(429).body(Map.of(
                    "success", false,
                    "message", "Bạn đã tra cứu quá nhiều lần. Vui lòng thử lại sau"
            ));
        }
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
            log.error("Không thể tra cứu đơn hàng", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Không thể tra cứu đơn hàng lúc này"
            ));
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
            Map<String, Object> customer = null;
            if (order.getKhachHang() != null) {
                customer = new LinkedHashMap<>();
                customer.put("hoVaTen", order.getKhachHang().getHoVaTen());
                customer.put("soDienThoai", order.getKhachHang().getSoDienThoai());
                customer.put("email", order.getKhachHang().getEmail());
            }
            result.put("khachHang", customer);
            result.put("trackingHistory", trackingHistory.stream()
                    .map(this::trackingToMap)
                    .toList());
            result.put("ngayTao", order.getNgayTao());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Không thể tải tracking đơn hàng {}", hoaDonId, e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Không thể tải hành trình đơn hàng lúc này"));
        }
    }

    @PutMapping("/{hoaDonId}/tracking/update")
    public ResponseEntity<?> updateOrderTracking(@PathVariable Integer hoaDonId,
                                                 @RequestBody Map<String, Object> body,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Dữ liệu tracking không hợp lệ"));
        }
        String trackingStatus = cleanText(body.get("trangThaiTracking"));
        Byte numericStatus = numericStatus(trackingStatus);
        if (numericStatus == null || numericStatus == STATUS_PAYMENT_FAILED) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Trạng thái tracking không hợp lệ"
            ));
        }
        Actor actor = actorFromAuth(authHeader);
        try {
            HoaDon order = orderStatusService.transition(
                    hoaDonId,
                    numericStatus,
                    cleanText(body.get("moTa")),
                    actor.name(),
                    actor.role()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "data", toDetailMap(order)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private List<Map<String, Object>> toMaps(List<HoaDon> orders) {
        if (orders.isEmpty()) return List.of();
        Map<Integer, Long> itemCounts = new HashMap<>();
        List<Integer> orderIds = orders.stream().map(HoaDon::getId).toList();
        for (Object[] row : hoaDonCtRepo.countItemsByOrderIds(orderIds)) {
            itemCounts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        Map<Integer, com.zestia.datn.zestia.entity.YeuCauDoiTra> latestReturns = new HashMap<>();
        for (var request : returnRequestRepo.findByHoaDonIdInOrderByNgayTaoDesc(orderIds)) {
            latestReturns.putIfAbsent(request.getHoaDon().getId(), request);
        }
        return orders.stream()
                .map(order -> withReturnStatus(
                        toMap(order, itemCounts.getOrDefault(order.getId(), 0L)),
                        latestReturns.get(order.getId())))
                .toList();
    }

    private Map<String, Object> toMap(HoaDon hd) {
        return toMap(hd, hoaDonCtRepo.countByHoaDonId(hd.getId()));
    }

    private Map<String, Object> toMap(HoaDon hd, long soSanPham) {
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
        
        map.put("trangThai", hd.getTrangThai()); 
        map.put("trangThaiTracking", hd.getTrangThaiTracking()); 
        
        map.put("diaChiGiaoHang", hd.getDiaChiGiaoHang());
        map.put("hinhThucNhanHang", hd.getHinhThucNhanHang()); 
        map.put("ghiChu", hd.getGhiChu());
        map.put("thongTinHoanTien", hd.getThongTinHoanTien());
        map.put("ngayTao", hd.getNgayTao());
        map.put("ngayGiaoHangDuKien", hd.getNgayGiaoHangDuKien()); 
        
        return map;
    }

    private Map<String, Object> toDetailMap(HoaDon hd) {
        Map<String, Object> map = withReturnStatus(toMap(hd),
                returnRequestRepo.findFirstByHoaDonIdOrderByNgayTaoDesc(hd.getId()).orElse(null));
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : hd.getEmailKhachHang());
        
        List<HoaDonChiTiet> chiTiets = hoaDonCtRepo.findByHoaDonId(hd.getId());
        Map<Integer, Integer> returnedQuantities = new HashMap<>();
        List<Integer> orderDetailIds = chiTiets.stream().map(HoaDonChiTiet::getId).toList();
        if (!orderDetailIds.isEmpty()) {
            for (Object[] row : returnRequestRepo.sumQuantitiesByOrderDetailsAndStatuses(
                    orderDetailIds, ReturnExchangeService.QUANTITY_CONSUMING_STATUSES)) {
                returnedQuantities.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            }
        }
        List<Integer> productIds = chiTiets.stream()
                .filter(item -> item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getSanPham() != null)
                .map(item -> item.getSanPhamChiTiet().getSanPham().getId())
                .distinct()
                .toList();
        Map<Integer, String> firstImages = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (Anh image : anhRepo.findBySanPhamIdInAndTrangThaiOrderByIdAsc(productIds, (byte) 1)) {
                if (image.getSanPham() != null) {
                    firstImages.putIfAbsent(image.getSanPham().getId(), image.getAnhUrl());
                }
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonChiTiet ct : chiTiets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ct.getId());
            if (ct.getSanPhamChiTiet() != null) {
                item.put("variantId", ct.getSanPhamChiTiet().getId());
                item.put("productId", ct.getSanPhamChiTiet().getSanPham() != null
                        ? ct.getSanPhamChiTiet().getSanPham().getId() : null);
                item.put("tenSanPham", firstNonBlank(ct.getTenSanPhamSnapshot(),
                        ct.getSanPhamChiTiet().getSanPham() != null
                                ? ct.getSanPhamChiTiet().getSanPham().getTenSanPham() : null));
                item.put("maSanPham", firstNonBlank(ct.getMaSanPhamSnapshot(),
                        ct.getSanPhamChiTiet().getSanPham() != null
                                ? ct.getSanPhamChiTiet().getSanPham().getMaSanPham() : null));
                item.put("mauSac", firstNonBlank(ct.getMauSacSnapshot(),
                        ct.getSanPhamChiTiet().getMauSac() != null
                                ? ct.getSanPhamChiTiet().getMauSac().getTenMauSac() : null));
                item.put("maHex", ct.getSanPhamChiTiet().getMauSac() != null 
                        ? ct.getSanPhamChiTiet().getMauSac().getMaHex() : null);
                item.put("kichThuoc", firstNonBlank(ct.getKichThuocSnapshot(),
                        ct.getSanPhamChiTiet().getKichThuoc() != null
                                ? ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc() : null));
                
                if (ct.getSanPhamChiTiet().getSanPham() != null) {
                    var sp = ct.getSanPhamChiTiet().getSanPham();
                    String variantImage = firstNonBlank(
                            ct.getAnhUrlSnapshot(),
                            ct.getSanPhamChiTiet().getAnhUrl()
                    );
                    String img = variantImage != null
                            ? variantImage
                            : (firstImages.get(sp.getId()) != null
                                    ? firstImages.get(sp.getId())
                                    : SanPhamController.getFallbackImage(sp));
                    item.put("anhUrl", img);
                }
            }
            item.put("soLuong", ct.getSoLuong());
            int returnedQuantity = returnedQuantities.getOrDefault(ct.getId(), 0);
            item.put("soLuongDaDoiTra", returnedQuantity);
            item.put("soLuongConLaiDoiTra", Math.max(0,
                    Optional.ofNullable(ct.getSoLuong()).orElse(0) - returnedQuantity));
            item.put("donGia", ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
            item.put("thanhTien", ct.getThanhTien() != null
                    ? ct.getThanhTien()
                    : Optional.ofNullable(ct.getDonGia()).orElse(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(Optional.ofNullable(ct.getSoLuong()).orElse(0))));
            item.put("phanTramGiam", ct.getPhanTramGiam());
            items.add(item);
        }
        map.put("chiTiets", items);
        map.put("auditLogs", auditLogRepo.findByHoaDonIdOrderByNgayTaoDesc(hd.getId()).stream()
                .map(this::auditToMap)
                .toList());
        return map;
    }

    private Map<String, Object> withReturnStatus(Map<String, Object> map,
                                                   com.zestia.datn.zestia.entity.YeuCauDoiTra request) {
        map.put("returnRequestStatus", request != null ? request.getTrangThai() : null);
        map.put("returnRequestType", request != null ? request.getLoaiYeuCau() : null);
        map.put("returnRefundAmount", request != null ? request.getSoTienHoan() : null);
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

    private Byte numericStatus(String status) {
        if (status == null) return null;
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "pending" -> 0;
            case "confirmed" -> 1;
            case "processing" -> 2;
            case "shipped" -> 3;
            case "delivered" -> 4;
            case "cancelled" -> 5;
            case "failed" -> 6;
            case "payment_failed" -> 7;
            default -> null;
        };
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

    private boolean matchesGuestOrder(HoaDon hd, Map<String, Object> body) {
        String orderCode = cleanText(hd.getMaHoaDon());
        String orderPhone = cleanText(hd.getSoDienThoai());
        String providedCode = cleanText(body != null ? body.get("maHoaDon") : null);
        String providedPhone = cleanText(body != null ? body.get("soDienThoai") : null);
        return orderCode != null
                && orderPhone != null
                && providedCode != null
                && providedPhone != null
                && orderCode.equalsIgnoreCase(providedCode)
                && orderPhone.equals(providedPhone);
    }

    private ResponseEntity<?> validateGuestCancellationStatus(HoaDon hd) {
        if (hd.getTrangThai() != null && hd.getTrangThai() == STATUS_CANCELLED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng này đã được hủy trước đó"));
        }
        if (Boolean.TRUE.equals(hd.getDaThanhToan())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Đơn đã thanh toán không thể hủy trực tiếp. Vui lòng đăng nhập để yêu cầu trả hàng và hoàn tiền"
            ));
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Đơn hàng đã được xác nhận hoặc đang vận chuyển, không thể hủy ở thời điểm này"
            ));
        }
        return null;
    }

    private String resolveOrderEmail(HoaDon hd) {
        if (hd.getKhachHang() != null) {
            String customerEmail = cleanText(hd.getKhachHang().getEmail());
            if (customerEmail != null) return customerEmail;
        }
        return cleanText(hd.getEmailKhachHang());
    }

    private String maskEmail(String email) {
        int at = email != null ? email.indexOf('@') : -1;
        if (at <= 0) return "***";
        String local = email.substring(0, at);
        String visible = local.substring(0, Math.min(2, local.length()));
        return visible + "***" + email.substring(at);
    }

    private void clearCancellationOtp(HoaDon hd) {
        hd.setHuyDonOtpHash(null);
        hd.setHuyDonOtpHetHan(null);
        hd.setHuyDonOtpSoLanSai(null);
        hd.setHuyDonOtpGuiLuc(null);
    }

    private static String cleanText(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    @GetMapping("/my-orders/paged")
    public ResponseEntity<?> getMyOrdersPage(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "6") int size,
                                             @RequestParam(defaultValue = "all") String tab,
                                             @RequestParam(required = false) String q,
                                             Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        String normalizedTab = tab == null ? "all" : tab.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("all", "unpaid", "pending", "processing", "completed", "cancelled").contains(normalizedTab)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhóm trạng thái đơn hàng không hợp lệ"));
        }
        String keyword = q == null || q.isBlank() ? null : q.trim();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(48, Math.max(1, size));
        var result = hoaDonRepo.findCustomerPage(
                customer.getId(), keyword, normalizedTab,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "ngayTao", "id"))
        );

        Map<String, Long> counts = customerOrderCounts(customer.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", toMaps(result.getContent()));
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("counts", counts);
        response.put("loyalty", loyaltyService.toLoyaltySummaryMap(customer));
        return ResponseEntity.ok(response);
    }

    private Map<String, Long> customerOrderCounts(Integer customerId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String key : List.of("all", "unpaid", "pending", "processing", "completed", "cancelled")) {
            counts.put(key, 0L);
        }
        for (Object[] row : hoaDonRepo.countCustomerOrdersByState(customerId)) {
            int status = row[0] != null ? ((Number) row[0]).intValue() : 0;
            String method = row[1] != null ? String.valueOf(row[1]) : "COD";
            boolean paid = Boolean.TRUE.equals(row[2]);
            long count = ((Number) row[3]).longValue();
            counts.compute("all", (key, value) -> value + count);
            if (status == 0 && Set.of("MOMO", "ZALOPAY").contains(method) && !paid) {
                counts.compute("unpaid", (key, value) -> value + count);
            } else if (status == 0 && "COD".equals(method)) {
                counts.compute("pending", (key, value) -> value + count);
            } else if (status >= 1 && status <= 3) {
                counts.compute("processing", (key, value) -> value + count);
            } else if (status == 4) {
                counts.compute("completed", (key, value) -> value + count);
            } else if (status >= 5 && status <= 7) {
                counts.compute("cancelled", (key, value) -> value + count);
            }
        }
        return counts;
    }

    private static String firstNonBlank(String preferred, String fallback) {
        String value = cleanText(preferred);
        return value != null ? value : cleanText(fallback);
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
                || "Nhân viên".equalsIgnoreCase(role);
    }

    private static String clientIp(HttpServletRequest request) {
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record Actor(String name, String role) {}

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }
}
