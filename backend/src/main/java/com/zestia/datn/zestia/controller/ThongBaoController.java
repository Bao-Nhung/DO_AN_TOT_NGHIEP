package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.service.AnnouncementEmailDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
@RequiredArgsConstructor
public class ThongBaoController {
    private static final byte DRAFT = 0;
    private static final byte ACTIVE = 1;
    private static final byte SCHEDULED = 2;

    private final ThongBaoRepository notificationRepository;
    private final AnnouncementEmailDispatchService emailDispatchService;

    @GetMapping
    public Map<String, Object> getAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) Byte status) {
        if (status != null && status != DRAFT && status != ACTIVE && status != SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái thông báo không hợp lệ");
        }
        var result = notificationRepository.findAdminPage(
                cleanFilter(q), cleanFilter(type), status, pageRequest(page, size));
        return pageResponse(result);
    }

    @GetMapping("/active")
    public Map<String, Object> getActive(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "8") int size,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(required = false) String type) {
        var result = notificationRepository.findPublicPage(
                cleanFilter(q), cleanFilter(type), ACTIVE, pageRequest(page, size));
        Map<String, Object> response = pageResponse(result);
        response.put("counts", typeCounts(notificationRepository.countPublicByType(ACTIVE)));
        return response;
    }

    @PostMapping
    public ThongBao create(@RequestBody Map<String, Object> body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu thông báo không hợp lệ");
        }
        ThongBao notification = new ThongBao();
        notification.setTieuDe(requiredText(body, "tieuDe", "Tiêu đề", 255));
        notification.setNoiDung(requiredText(body, "noiDung", "Nội dung", 10_000));
        notification.setLoai(optionalText(body.get("loai"), 50));
        notification.setGuiEmail(flag(body.get("guiEmail"), (byte) 0));
        notification.setDaGui((byte) 0);
        notification.setNgayTao(LocalDateTime.now());
        applySchedule(notification, state(body.get("trangThai"), ACTIVE), dateTime(body.get("ngayGui")));

        ThongBao saved = notificationRepository.save(notification);
        dispatchWhenReady(saved);
        return saved;
    }

    @PutMapping("/{id}")
    public ThongBao update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu thông báo không hợp lệ");
        }
        ThongBao notification = notificationRepository.findByIdAndKhachHangIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
        if (Byte.valueOf((byte) 2).equals(notification.getDaGui())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Thông báo đang được gửi email, vui lòng thử lại sau");
        }

        if (body.containsKey("tieuDe")) {
            notification.setTieuDe(requiredText(body, "tieuDe", "Tiêu đề", 255));
        }
        if (body.containsKey("noiDung")) {
            notification.setNoiDung(requiredText(body, "noiDung", "Nội dung", 10_000));
        }
        if (body.containsKey("loai")) {
            notification.setLoai(optionalText(body.get("loai"), 50));
        }

        byte oldEmailFlag = notification.getGuiEmail() != null ? notification.getGuiEmail() : 0;
        byte emailFlag = body.containsKey("guiEmail")
                ? flag(body.get("guiEmail"), oldEmailFlag)
                : oldEmailFlag;
        notification.setGuiEmail(emailFlag);
        if (oldEmailFlag == 0 && emailFlag == 1) notification.setDaGui((byte) 0);

        byte requestedState = body.containsKey("trangThai")
                ? state(body.get("trangThai"), notification.getTrangThai())
                : notification.getTrangThai();
        LocalDateTime requestedTime = body.containsKey("ngayGui")
                ? dateTime(body.get("ngayGui"))
                : notification.getNgayGui();
        applySchedule(notification, requestedState, requestedTime);
        if (requestedState == DRAFT) notification.setDaGui((byte) 0);

        ThongBao saved = notificationRepository.save(notification);
        dispatchWhenReady(saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        ThongBao notification = notificationRepository.findByIdAndKhachHangIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
        if (Byte.valueOf((byte) 2).equals(notification.getDaGui())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Thông báo đang được gửi email, chưa thể xóa");
        }
        notificationRepository.delete(notification);
        return ResponseEntity.noContent().build();
    }

    private void applySchedule(ThongBao notification, byte requestedState, LocalDateTime requestedTime) {
        if (requestedState == SCHEDULED) {
            if (requestedTime == null || !requestedTime.isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày giờ hẹn gửi phải ở trong tương lai");
            }
            notification.setTrangThai(SCHEDULED);
            notification.setNgayGui(requestedTime);
            return;
        }
        notification.setTrangThai(requestedState);
        notification.setNgayGui(null);
    }

    private void dispatchWhenReady(ThongBao notification) {
        if (Byte.valueOf(ACTIVE).equals(notification.getTrangThai())
                && Byte.valueOf((byte) 1).equals(notification.getGuiEmail())
                && (notification.getDaGui() == null || notification.getDaGui() == 0)) {
            emailDispatchService.dispatch(notification.getId());
        }
    }

    private String requiredText(Map<String, Object> body, String key, String label, int maxLength) {
        String value = optionalText(body != null ? body.get(key) : null, maxLength);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập " + label.toLowerCase());
        }
        return value;
    }

    private String optionalText(Object raw, int maxLength) {
        if (raw == null) return null;
        String value = String.valueOf(raw).trim();
        if (value.isEmpty()) return null;
        if (value.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung vượt quá " + maxLength + " ký tự");
        }
        return value;
    }

    private byte state(Object raw, Byte fallback) {
        int value = integer(raw, fallback != null ? fallback : ACTIVE);
        if (value < DRAFT || value > SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái thông báo không hợp lệ");
        }
        return (byte) value;
    }

    private byte flag(Object raw, byte fallback) {
        int value = integer(raw, fallback);
        if (value != 0 && value != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá trị tùy chọn gửi email không hợp lệ");
        }
        return (byte) value;
    }

    private int integer(Object raw, int fallback) {
        if (raw == null) return fallback;
        if (raw instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu số không hợp lệ");
        }
    }

    private LocalDateTime dateTime(Object raw) {
        if (raw == null || String.valueOf(raw).isBlank()) return null;
        try {
            return LocalDateTime.parse(String.valueOf(raw));
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày giờ hẹn gửi không hợp lệ");
        }
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "ngayTao", "id")
        );
    }

    private String cleanFilter(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private Map<String, Object> pageResponse(org.springframework.data.domain.Page<ThongBao> page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", page.getContent());
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }

    private Map<String, Long> typeCounts(java.util.List<Object[]> rows) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("all", 0L);
        for (Object[] row : rows) {
            String type = row[0] != null ? String.valueOf(row[0]) : "Khac";
            long count = ((Number) row[1]).longValue();
            counts.put(type, count);
            counts.put("all", counts.get("all") + count);
        }
        return counts;
    }
}
