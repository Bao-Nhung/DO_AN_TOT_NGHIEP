package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
@RequiredArgsConstructor
@Slf4j
public class ThongBaoController {

    private final NotificationService notificationService;

    /**
     * Lấy danh sách thông báo của khách hàng
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo
     */
    @GetMapping("/customer/{khachHangId}")
    public ResponseEntity<?> getNotifications(@PathVariable Integer khachHangId) {
        try {
            List<ThongBao> notifications = notificationService.getNotificationsByCustomerId(khachHangId);
            List<Map<String, Object>> result = notifications.stream()
                    .map(this::toMap)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result,
                    "total", result.size()
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông báo: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách thông báo chưa đọc
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo chưa đọc
     */
    @GetMapping("/customer/{khachHangId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Integer khachHangId) {
        try {
            List<ThongBao> notifications = notificationService.getUnreadNotifications(khachHangId);
            List<Map<String, Object>> result = notifications.stream()
                    .map(this::toMap)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result,
                    "total", result.size()
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông báo chưa đọc: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy số lượng thông báo chưa đọc
     * @param khachHangId ID khách hàng
     * @return Số lượng thông báo chưa đọc
     */
    @GetMapping("/customer/{khachHangId}/unread-count")
    public ResponseEntity<?> getUnreadCount(@PathVariable Integer khachHangId) {
        try {
            long unreadCount = notificationService.getUnreadCount(khachHangId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "unreadCount", unreadCount
            ));
        } catch (Exception e) {
            log.error("Lỗi khi lấy số thông báo chưa đọc: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Đánh dấu thông báo đã đọc
     * @param thongBaoId ID thông báo
     * @return Kết quả
     */
    @PutMapping("/{thongBaoId}/mark-as-read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer thongBaoId) {
        try {
            notificationService.markAsRead(thongBaoId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đánh dấu đã đọc thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu thông báo đã đọc: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Đánh dấu tất cả thông báo của khách hàng đã đọc
     * @param khachHangId ID khách hàng
     * @return Kết quả
     */
    @PutMapping("/customer/{khachHangId}/mark-all-as-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable Integer khachHangId) {
        try {
            notificationService.markAllAsRead(khachHangId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đánh dấu tất cả đã đọc thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tất cả thông báo đã đọc: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Xóa thông báo
     * @param thongBaoId ID thông báo
     * @return Kết quả
     */
    @DeleteMapping("/{thongBaoId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer thongBaoId) {
        try {
            notificationService.deleteNotification(thongBaoId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa thông báo thành công"
            ));
        } catch (Exception e) {
            log.error("Lỗi khi xóa thông báo: ", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> toMap(ThongBao tb) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tb.getId());
        map.put("loaiThongBao", tb.getLoaiThongBao());
        map.put("tieuDe", tb.getTieuDe());
        map.put("noiDung", tb.getNoiDung());
        map.put("daDoc", tb.getDaDoc() == 1);
        map.put("ngayTao", tb.getNgayTao());
        if (tb.getHoaDon() != null) {
            map.put("hoaDonId", tb.getHoaDon().getId());
            map.put("maHoaDon", tb.getHoaDon().getMaHoaDon());
        }
        return map;
    }
}