package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.entity.ThongBaoDaDoc;
import com.zestia.datn.zestia.repository.ThongBaoDaDocRepository;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.service.CurrentCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/customer-notifications")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final ThongBaoRepository notificationRepo;
    private final ThongBaoDaDocRepository readRepo;
    private final CurrentCustomerService currentCustomerService;

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> active(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "8") int size,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) String type,
                                      Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        String keyword = q == null || q.isBlank() ? null : q.trim();
        String typeFilter = type == null || type.isBlank() ? null : type.trim();
        var result = notificationRepo.findVisibleForCustomerPage(
                customer.getId(),
                (byte) 1,
                keyword,
                typeFilter,
                PageRequest.of(
                        Math.max(0, page),
                        Math.min(100, Math.max(1, size)),
                        Sort.by(Sort.Direction.DESC, "ngayTao", "id")
                )
        );
        List<ThongBao> notifications = result.getContent();
        List<Integer> ids = notifications.stream().map(ThongBao::getId).toList();
        Set<Integer> readIds = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(readRepo.findReadNotificationIds(customer.getId(), ids));
        List<Map<String, Object>> content = notifications.stream()
                .map(notification -> toMap(notification, readIds.contains(notification.getId())))
                .toList();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("all", 0L);
        for (Object[] row : notificationRepo.countVisibleByType(customer.getId(), (byte) 1)) {
            String notificationType = row[0] != null ? String.valueOf(row[0]) : "Khac";
            long count = ((Number) row[1]).longValue();
            counts.put(notificationType, count);
            counts.put("all", counts.get("all") + count);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("counts", counts);
        response.put("unreadCount", notificationRepo.countUnreadForCustomer(customer.getId(), (byte) 1));
        return response;
    }

    @PutMapping("/{id}/read")
    @Transactional
    public Map<String, Object> markRead(@PathVariable Integer id, Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        ThongBao notification = notificationRepo.findById(id)
                .filter(item -> Byte.valueOf((byte) 1).equals(item.getTrangThai()))
                .filter(item -> item.getKhachHang() == null
                        || Objects.equals(item.getKhachHang().getId(), customer.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
        if (!readRepo.existsByKhachHangIdAndThongBaoId(customer.getId(), id)) {
            readRepo.save(ThongBaoDaDoc.builder()
                    .khachHang(customer)
                    .thongBao(notification)
                    .ngayDoc(LocalDateTime.now())
                    .build());
        }
        return Map.of("id", id, "read", true);
    }

    @PutMapping("/read-all")
    @Transactional
    public Map<String, Object> markAllRead(Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        List<ThongBao> active = notificationRepo.findVisibleForCustomer(customer.getId(), (byte) 1);
        List<Integer> ids = active.stream().map(ThongBao::getId).toList();
        Set<Integer> existing = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(readRepo.findReadNotificationIds(customer.getId(), ids));
        List<ThongBaoDaDoc> newRows = active.stream()
                .filter(notification -> !existing.contains(notification.getId()))
                .map(notification -> ThongBaoDaDoc.builder()
                        .khachHang(customer)
                        .thongBao(notification)
                        .ngayDoc(LocalDateTime.now())
                        .build())
                .toList();
        readRepo.saveAll(newRows);
        return Map.of("updated", newRows.size());
    }

    private Map<String, Object> toMap(ThongBao notification, boolean read) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", notification.getId());
        map.put("tieuDe", notification.getTieuDe());
        map.put("noiDung", notification.getNoiDung());
        map.put("loai", notification.getLoai());
        map.put("ngayTao", notification.getNgayTao());
        map.put("read", read);
        return map;
    }
}
