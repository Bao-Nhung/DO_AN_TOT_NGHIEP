package com.zestia.datn.zestia.scheduler;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.service.AnnouncementEmailDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private static final PageRequest ACTIVATION_BATCH = PageRequest.of(0, 100, Sort.by("id"));
    private static final PageRequest EMAIL_BATCH = PageRequest.of(0, 20, Sort.by("id"));

    private final ThongBaoRepository notificationRepository;
    private final AnnouncementEmailDispatchService emailDispatchService;

    @Scheduled(fixedDelay = 30_000)
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        for (ThongBao notification : notificationRepository
                .findByTrangThaiAndKhachHangIsNullAndNgayGuiBefore((byte) 2, now, ACTIVATION_BATCH)) {
            notification.setTrangThai((byte) 1);
            notification.setNgayGui(null);
            notificationRepository.save(notification);
            log.info("Đã kích hoạt thông báo hẹn giờ {}", notification.getId());
        }

        if (emailDispatchService.isConfigured()) {
            for (ThongBao notification : notificationRepository.findPendingEmailAnnouncements(EMAIL_BATCH)) {
                emailDispatchService.dispatch(notification.getId());
            }
        }
    }
}
