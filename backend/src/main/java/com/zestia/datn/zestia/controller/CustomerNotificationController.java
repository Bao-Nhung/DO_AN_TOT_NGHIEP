package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.entity.ThongBaoDaDoc;
import com.zestia.datn.zestia.repository.ThongBaoDaDocRepository;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.service.CurrentCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public List<Map<String, Object>> active(Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        List<ThongBao> notifications = notificationRepo.findByTrangThaiOrderByNgayTaoDesc((byte) 1);
        List<Integer> ids = notifications.stream().map(ThongBao::getId).toList();
        Set<Integer> readIds = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(readRepo.findReadNotificationIds(customer.getId(), ids));
        return notifications.stream()
                .map(notification -> toMap(notification, readIds.contains(notification.getId())))
                .toList();
    }

    @PutMapping("/{id}/read")
    @Transactional
    public Map<String, Object> markRead(@PathVariable Integer id, Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        ThongBao notification = notificationRepo.findById(id)
                .filter(item -> Byte.valueOf((byte) 1).equals(item.getTrangThai()))
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
        List<ThongBao> active = notificationRepo.findByTrangThaiOrderByNgayTaoDesc((byte) 1);
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
