package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.service.ShiftAccessService;
import com.zestia.datn.zestia.service.ShiftReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lich-lam-viec")
@RequiredArgsConstructor
public class LichLamViecController {

    private final LichLamViecRepository lichLamViecRepo;
    private final NhanVienRepository nhanVienRepo;
    private final JwtUtil jwtUtil;
    private final ShiftAccessService shiftAccessService;
    private final ShiftReportService shiftReportService;

    @GetMapping
    public List<Map<String, Object>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer nhanVienId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Integer tokenUserId = extractStaffUserId(authHeader);
        if (!isAdmin(authHeader) && tokenUserId == null) return List.of();
        if (!isAdmin(authHeader) && tokenUserId != null) {
            nhanVienId = tokenUserId;
        }

        List<LichLamViec> data;
        if (nhanVienId != null) {
            data = lichLamViecRepo.findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(nhanVienId);
        } else if (startDate != null && endDate != null) {
            data = lichLamViecRepo.findByNgayLamBetweenOrderByNgayLamAscGioBatDauAsc(startDate, endDate);
        } else {
            data = lichLamViecRepo.findAll();
        }

        return data.stream()
                .filter(item -> startDate == null || !item.getNgayLam().isBefore(startDate))
                .filter(item -> endDate == null || !item.getNgayLam().isAfter(endDate))
                .map(this::toMap)
                .toList();
    }

    private Integer extractStaffUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return null;
            return toInt(jwtUtil.extractClaims(token).get("userId"));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        try {
            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) return false;
            String role = jwtUtil.extractClaims(token).get("role", String.class);
            return "Admin".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/nhan-vien")
    public List<Map<String, Object>> getNhanVien() {
        return nhanVienRepo.findAll().stream()
                .filter(nv -> nv.getTinhTrangLamViec() == null || nv.getTinhTrangLamViec() == 1)
                .map(nv -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", nv.getId());
                    map.put("maNhanVien", nv.getMaNhanVien());
                    map.put("hoVaTen", nv.getHoVaTen());
                    map.put("email", nv.getEmail());
                    map.put("soDienThoai", nv.getSoDienThoai());
                    return map;
                })
                .toList();
    }

    @GetMapping("/work-status")
    public ResponseEntity<?> getWorkStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (isAdmin(authHeader)) {
            return ResponseEntity.ok(Map.of(
                    "canOperate", true,
                    "reason", "Tài khoản admin không bị giới hạn theo ca",
                    "todayShifts", List.of()
            ));
        }
        Integer employeeId = extractStaffUserId(authHeader);
        if (employeeId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Không xác định được tài khoản nhân viên"));
        }
        ShiftAccessService.WorkStatus status = shiftAccessService.getWorkStatus(employeeId, LocalDateTime.now());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("canOperate", status.canOperate());
        response.put("reason", status.reason());
        response.put("activeShift", status.activeShift() != null ? toMap(status.activeShift()) : null);
        response.put("relevantShift", status.relevantShift() != null ? toMap(status.relevantShift()) : null);
        response.put("todayShifts", status.todayShifts().stream().map(this::toMap).toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer nhanVienId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        DateRange range = resolveRange(startDate, endDate);
        Integer requesterId = extractStaffUserId(authHeader);
        if (!isAdmin(authHeader)) {
            if (requesterId == null) return ResponseEntity.status(401).body(Map.of("error", "Không xác định được nhân viên"));
            nhanVienId = requesterId;
        }
        try {
            return ResponseEntity.ok(shiftReportService.build(range.start(), range.end(), nhanVienId).stream()
                    .map(this::reportMap)
                    .toList());
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
        }
    }

    @GetMapping("/history/export")
    public ResponseEntity<?> exportHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer nhanVienId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        DateRange range = resolveRange(startDate, endDate);
        Integer requesterId = extractStaffUserId(authHeader);
        if (!isAdmin(authHeader)) {
            if (requesterId == null) return ResponseEntity.status(401).body(Map.of("error", "Không xác định được nhân viên"));
            nhanVienId = requesterId;
        }
        try {
            byte[] file = shiftReportService.exportExcel(shiftReportService.build(range.start(), range.end(), nhanVienId));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=lich-su-ca-lam-" + range.start() + "-" + range.end() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(file.length)
                    .body(file);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LichLamViec lich) {
        if (lich.getNhanVien() == null || lich.getNhanVien().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn nhân viên"));
        }
        NhanVien nhanVien = nhanVienRepo.findById(lich.getNhanVien().getId()).orElse(null);
        if (nhanVien == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nhân viên không tồn tại"));
        }
        String validationError = validateSchedule(lich);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("message", validationError));
        }
        if (hasOverlap(lich, null)) {
            return ResponseEntity.status(409).body(Map.of("message", "Nhân viên đã có ca làm trùng thời gian"));
        }
        lich.setNhanVien(nhanVien);
        lich.setNgayTao(LocalDateTime.now());
        if (lich.getTrangThai() == null) lich.setTrangThai(ShiftAccessService.STATUS_PENDING);
        if (lich.getTrangThai() == ShiftAccessService.STATUS_CONFIRMED && lich.getThoiGianXacNhan() == null) {
            lich.setThoiGianXacNhan(LocalDateTime.now());
        }
        lich.setLyDoBaoBan(null);
        lich.setPhanHoiBaoBan(null);
        lich.setThoiGianBaoBan(null);
        lich.setThoiGianDuyet(null);
        lich.setNguoiDuyet(null);
        lich.setGioCheckIn(null);
        lich.setGioCheckOut(null);
        return ResponseEntity.ok(toMap(lichLamViecRepo.save(lich)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LichLamViec lich) {
        return lichLamViecRepo.findById(id).map(existing -> {
            if (existing.getGioCheckIn() != null || existing.getGioCheckOut() != null) {
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Không thể sửa ca đã chấm công; hãy giữ lịch sử để đối soát"
                ));
            }
            if (lich.getNhanVien() != null && lich.getNhanVien().getId() != null) {
                nhanVienRepo.findById(lich.getNhanVien().getId()).ifPresent(existing::setNhanVien);
            }
            if (lich.getNgayLam() != null) existing.setNgayLam(lich.getNgayLam());
            if (lich.getCaLam() != null) existing.setCaLam(lich.getCaLam());
            if (lich.getGioBatDau() != null) existing.setGioBatDau(lich.getGioBatDau());
            if (lich.getGioKetThuc() != null) existing.setGioKetThuc(lich.getGioKetThuc());
            if (lich.getGhiChu() != null) existing.setGhiChu(lich.getGhiChu());
            if (lich.getTrangThai() != null) {
                if (!validStatus(lich.getTrangThai())) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái ca làm không hợp lệ"));
                }
                existing.setTrangThai(lich.getTrangThai());
                if (lich.getTrangThai() == ShiftAccessService.STATUS_CONFIRMED && existing.getThoiGianXacNhan() == null) {
                    existing.setThoiGianXacNhan(LocalDateTime.now());
                }
            }
            String validationError = validateSchedule(existing);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(Map.of("message", validationError));
            }
            if (hasOverlap(existing, existing.getId())) {
                return ResponseEntity.status(409).body(Map.of("message", "Nhân viên đã có ca làm trùng thời gian"));
            }
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public ResponseEntity<?> confirmShift(@PathVariable Integer id,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            ResponseEntity<?> accessError = authorizeOwnShift(shift, authHeader);
            if (accessError != null) return accessError;
            if (shift.getTrangThai() == null || shift.getTrangThai() != ShiftAccessService.STATUS_PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ ca đang chờ xác nhận mới có thể xác nhận"));
            }
            if (shiftAccessService.isPast(shift, LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ca làm đã kết thúc, không thể xác nhận"));
            }
            shift.setTrangThai(ShiftAccessService.STATUS_CONFIRMED);
            shift.setThoiGianXacNhan(LocalDateTime.now());
            shift.setPhanHoiBaoBan(null);
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/unavailable")
    @Transactional
    public ResponseEntity<?> reportUnavailable(@PathVariable Integer id,
                                               @RequestBody Map<String, Object> body,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            ResponseEntity<?> accessError = authorizeOwnShift(shift, authHeader);
            if (accessError != null) return accessError;
            String reason = cleanText(body.get("lyDo"));
            if (reason == null || reason.length() < 10 || reason.length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("error", "Lý do báo bận phải từ 10 đến 500 ký tự"));
            }
            if (shiftAccessService.isPast(shift, LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ca làm đã kết thúc, không thể báo bận"));
            }
            if (shift.getGioCheckIn() != null && shift.getGioCheckOut() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã check-in. Vui lòng check-out và liên hệ admin nếu cần rời ca"));
            }
            byte currentStatus = shift.getTrangThai() != null ? shift.getTrangThai() : ShiftAccessService.STATUS_PENDING;
            if (currentStatus != ShiftAccessService.STATUS_PENDING
                    && currentStatus != ShiftAccessService.STATUS_CONFIRMED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ca làm không còn ở trạng thái có thể báo bận"));
            }

            shift.setLyDoBaoBan(reason);
            shift.setThoiGianBaoBan(LocalDateTime.now());
            shift.setPhanHoiBaoBan(null);
            if (currentStatus == ShiftAccessService.STATUS_PENDING) {
                shift.setTrangThai(ShiftAccessService.STATUS_UNAVAILABLE);
                shift.setThoiGianDuyet(LocalDateTime.now());
                shift.setNguoiDuyet("Tự động - báo trước xác nhận");
            } else {
                shift.setTrangThai(ShiftAccessService.STATUS_UNAVAILABLE_PENDING);
                shift.setThoiGianDuyet(null);
                shift.setNguoiDuyet(null);
            }
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/review-unavailable")
    @Transactional
    public ResponseEntity<?> reviewUnavailable(@PathVariable Integer id,
                                               @RequestBody Map<String, Object> body,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Chỉ admin được duyệt yêu cầu báo bận"));
        }
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            if (shift.getTrangThai() == null || shift.getTrangThai() != ShiftAccessService.STATUS_UNAVAILABLE_PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ca làm không có yêu cầu báo bận đang chờ duyệt"));
            }
            boolean approved = Boolean.TRUE.equals(body.get("approved"));
            String feedback = cleanText(body.get("phanHoi"));
            if (!approved && (feedback == null || feedback.length() < 5)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập lý do từ chối"));
            }
            shift.setTrangThai(approved
                    ? ShiftAccessService.STATUS_UNAVAILABLE
                    : ShiftAccessService.STATUS_CONFIRMED);
            shift.setPhanHoiBaoBan(feedback);
            shift.setThoiGianDuyet(LocalDateTime.now());
            shift.setNguoiDuyet(extractUsername(authHeader));
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/check-in")
    @Transactional
    public ResponseEntity<?> checkIn(@PathVariable Integer id,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            ResponseEntity<?> accessError = authorizeOwnShift(shift, authHeader);
            if (accessError != null) return accessError;
            LocalDateTime now = LocalDateTime.now();
            if (!shiftAccessService.canCheckIn(shift, now)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Chỉ được check-in từ 30 phút trước giờ bắt đầu đến trước khi ca kết thúc"
                ));
            }
            shift.setGioCheckIn(now);
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/check-out")
    @Transactional
    public ResponseEntity<?> checkOut(@PathVariable Integer id,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            ResponseEntity<?> accessError = authorizeOwnShift(shift, authHeader);
            if (accessError != null) return accessError;
            LocalDateTime now = LocalDateTime.now();
            if (!shiftAccessService.canCheckOut(shift, now)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ca làm này chưa check-in hoặc đã check-out"));
            }
            shift.setGioCheckOut(now);
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return lichLamViecRepo.findByIdForUpdate(id).map(shift -> {
            if (shift.getGioCheckIn() != null || shift.getGioCheckOut() != null
                    || (shift.getNgayLam() != null && shift.getNgayLam().isBefore(LocalDate.now()))) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Không thể xóa ca đã diễn ra hoặc đã chấm công"
                ));
            }
            shift.setTrangThai(ShiftAccessService.STATUS_UNAVAILABLE);
            shift.setLyDoBaoBan("Ca đã được hủy bởi quản trị viên");
            shift.setThoiGianBaoBan(LocalDateTime.now());
            return ResponseEntity.ok(toMap(lichLamViecRepo.save(shift)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private boolean hasOverlap(LichLamViec shift, Integer excludedId) {
        return shift.getNhanVien() != null
                && shift.getNhanVien().getId() != null
                && shift.getNgayLam() != null
                && shift.getGioBatDau() != null
                && shift.getGioKetThuc() != null
                && lichLamViecRepo.countOverlapping(
                        shift.getNhanVien().getId(),
                        shift.getNgayLam(),
                        shift.getGioBatDau(),
                        shift.getGioKetThuc(),
                        excludedId
                ) > 0;
    }

    private Map<String, Object> toMap(LichLamViec lich) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", lich.getId());
        map.put("ngayLam", lich.getNgayLam());
        map.put("caLam", lich.getCaLam());
        map.put("gioBatDau", lich.getGioBatDau());
        map.put("gioKetThuc", lich.getGioKetThuc());
        map.put("ghiChu", lich.getGhiChu());
        map.put("trangThai", lich.getTrangThai());
        map.put("lyDoBaoBan", lich.getLyDoBaoBan());
        map.put("phanHoiBaoBan", lich.getPhanHoiBaoBan());
        map.put("thoiGianXacNhan", lich.getThoiGianXacNhan());
        map.put("thoiGianBaoBan", lich.getThoiGianBaoBan());
        map.put("thoiGianDuyet", lich.getThoiGianDuyet());
        map.put("nguoiDuyet", lich.getNguoiDuyet());
        map.put("gioCheckIn", lich.getGioCheckIn());
        map.put("gioCheckOut", lich.getGioCheckOut());
        map.put("canCheckIn", shiftAccessService.canCheckIn(lich, LocalDateTime.now()));
        map.put("canCheckOut", shiftAccessService.canCheckOut(lich, LocalDateTime.now()));
        map.put("ngayTao", lich.getNgayTao());
        if (lich.getNhanVien() != null) {
            map.put("nhanVienId", lich.getNhanVien().getId());
            map.put("maNhanVien", lich.getNhanVien().getMaNhanVien());
            map.put("tenNhanVien", lich.getNhanVien().getHoVaTen());
            map.put("emailNhanVien", lich.getNhanVien().getEmail());
        }
        return map;
    }

    private Map<String, Object> reportMap(ShiftReportService.ShiftReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", report.id());
        map.put("ngayLam", report.date());
        map.put("nhanVienId", report.employeeId());
        map.put("maNhanVien", report.employeeCode());
        map.put("tenNhanVien", report.employeeName());
        map.put("caLam", report.shiftName());
        map.put("gioBatDau", report.scheduledStart());
        map.put("gioKetThuc", report.scheduledEnd());
        map.put("gioCheckIn", report.checkIn());
        map.put("gioCheckOut", report.checkOut());
        map.put("trangThai", report.status());
        map.put("soPhutLamViec", report.workedMinutes());
        map.put("soDon", report.orderCount());
        map.put("doanhThu", report.revenue());
        map.put("tienMatBanGiao", report.cashToHandover());
        map.put("tienChuyenKhoan", report.transferAmount());
        map.put("hoatDong", report.activities().stream().map(activity -> Map.of(
                "thoiGian", activity.time(),
                "loai", activity.type(),
                "noiDung", activity.description()
        )).toList());
        return map;
    }

    private ResponseEntity<?> authorizeOwnShift(LichLamViec shift, String authHeader) {
        if (isAdmin(authHeader)) return null;
        Integer employeeId = extractStaffUserId(authHeader);
        if (employeeId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Không xác định được tài khoản nhân viên"));
        }
        if (shift.getNhanVien() == null || !employeeId.equals(shift.getNhanVien().getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền thao tác ca làm của nhân viên khác"));
        }
        return null;
    }

    private String extractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return "Admin";
        try {
            return jwtUtil.extractUsername(authHeader.substring(7));
        } catch (Exception e) {
            return "Admin";
        }
    }

    private String cleanText(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String validateSchedule(LichLamViec shift) {
        if (shift.getNgayLam() == null || shift.getGioBatDau() == null || shift.getGioKetThuc() == null) {
            return "Vui lòng nhập đầy đủ ngày và giờ làm";
        }
        if (!shift.getGioKetThuc().isAfter(shift.getGioBatDau())) {
            return "Giờ kết thúc phải sau giờ bắt đầu";
        }
        if (shift.getTrangThai() != null && !validStatus(shift.getTrangThai())) {
            return "Trạng thái ca làm không hợp lệ";
        }
        return null;
    }

    private boolean validStatus(byte status) {
        return status >= ShiftAccessService.STATUS_PENDING
                && status <= ShiftAccessService.STATUS_UNAVAILABLE_PENDING;
    }

    private DateRange resolveRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? startDate : today.withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : today.withDayOfMonth(today.lengthOfMonth());
        return new DateRange(start, end);
    }

    private record DateRange(LocalDate start, LocalDate end) {
    }
}
