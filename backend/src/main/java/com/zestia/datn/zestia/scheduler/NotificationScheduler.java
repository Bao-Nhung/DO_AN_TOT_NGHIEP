package com.zestia.datn.zestia.scheduler;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import com.zestia.datn.zestia.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ThongBaoRepository thongBaoRepo;
    private final KhachHangRepository khachHangRepo;
    private final EmailService emailService;

    @Scheduled(fixedRate = 30000) // Chạy mỗi 30 giây để kiểm tra hẹn giờ gửi
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        // Tìm những thông báo ở trạng thái 2 (Hẹn giờ) mà ngày hẹn đã qua (<= hiện tại)
        List<ThongBao> scheduledList = thongBaoRepo.findByTrangThaiAndNgayGuiBefore((byte) 2, now);

        if (scheduledList.isEmpty()) {
            return;
        }

        log.info("Tìm thấy {} thông báo đã lên lịch cần kích hoạt", scheduledList.size());

        for (ThongBao tb : scheduledList) {
            try {
                // Kích hoạt trạng thái hiển thị (1 = Đang hiển thị)
                tb.setTrangThai((byte) 1);

                // Nếu được đánh dấu gửi email và chưa từng gửi email
                if (tb.getGuiEmail() != null && tb.getGuiEmail() == 1 && (tb.getDaGui() == null || tb.getDaGui() == 0)) {
                    tb.setDaGui((byte) 1);
                    thongBaoRepo.save(tb);

                    // Gửi email hàng loạt bất đồng bộ
                    sendBulkEmailsAsync(tb);
                } else {
                    thongBaoRepo.save(tb);
                }
                log.info("Đã kích hoạt thông báo thành công, ID: {}", tb.getId());
            } catch (Exception e) {
                log.error("Lỗi khi kích hoạt thông báo hẹn giờ ID " + tb.getId() + ": ", e);
            }
        }
    }

    private void sendBulkEmailsAsync(ThongBao thongBao) {
        CompletableFuture.runAsync(() -> {
            try {
                List<KhachHang> khachHangs = khachHangRepo.findAll();
                log.info("Bắt đầu gửi email hàng loạt cho thông báo ID {} tới {} khách hàng", thongBao.getId(), khachHangs.size());
                int count = 0;
                for (KhachHang kh : khachHangs) {
                    if (kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {
                        emailService.sendAnnouncementEmail(kh.getEmail(), kh.getHoVaTen(), thongBao.getTieuDe(), thongBao.getNoiDung());
                        count++;
                    }
                }
                log.info("Hoàn thành gửi email hàng loạt cho thông báo ID {}. Tổng số email đã gửi: {}", thongBao.getId(), count);
            } catch (Exception e) {
                log.error("Lỗi trong quá trình gửi email hàng loạt cho thông báo ID " + thongBao.getId() + ": ", e);
            }
        });
    }
}
