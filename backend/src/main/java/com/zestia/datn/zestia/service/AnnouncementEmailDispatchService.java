package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementEmailDispatchService {
    private final ThongBaoRepository notificationRepository;
    private final KhachHangRepository customerRepository;
    private final EmailService emailService;

    @Async
    public void dispatch(Integer notificationId) {
        if (notificationId == null || !claim(notificationId)) return;
        if (!emailService.isConfigured()) {
            finish(notificationId, (byte) 0);
            log.warn("Chưa cấu hình email; thông báo {} vẫn ở trạng thái chờ gửi", notificationId);
            return;
        }

        ThongBao notification = notificationRepository.findByIdAndKhachHangIsNull(notificationId).orElse(null);
        if (notification == null || !Byte.valueOf((byte) 1).equals(notification.getGuiEmail())) {
            finish(notificationId, (byte) 0);
            return;
        }

        int sent = 0;
        int failed = 0;
        int pageIndex = 0;
        Page<KhachHang> recipientPage;
        do {
            recipientPage = customerRepository.findAllWithEmail(
                    PageRequest.of(pageIndex, 100, Sort.by("id")));
            for (KhachHang customer : recipientPage.getContent()) {
                boolean success = emailService.sendAnnouncementEmailNow(
                        customer.getEmail(), customer.getHoVaTen(),
                        notification.getTieuDe(), notification.getNoiDung()
                );
                if (success) sent++;
                else failed++;
            }
            pageIndex++;
        } while (recipientPage.hasNext());

        // The row records that this campaign was processed. Per-recipient failures are logged
        // instead of retrying the whole campaign and sending duplicates to successful recipients.
        finish(notificationId, (byte) 1);
        log.info("Đã xử lý email cho thông báo {}: {} thành công, {} thất bại", notificationId, sent, failed);
    }

    protected boolean claim(Integer notificationId) {
        return notificationRepository.claimEmailDispatch(notificationId) == 1;
    }

    protected void finish(Integer notificationId, byte status) {
        notificationRepository.updateEmailDispatchStatus(notificationId, status);
    }

    public boolean isConfigured() {
        return emailService.isConfigured();
    }
}
