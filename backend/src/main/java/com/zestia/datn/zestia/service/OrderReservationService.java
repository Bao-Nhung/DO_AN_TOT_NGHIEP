package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReservationService {

    private static final int EXPIRY_BATCH_SIZE = 100;
    private static final byte STATUS_PENDING = 0;
    private static final byte STATUS_PAYMENT_FAILED = 7;
    private static final List<String> ONLINE_PAYMENT_METHODS = List.of("MOMO", "ZALOPAY");

    private final HoaDonRepository hoaDonRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final EmailService emailService;
    private final OrderInventoryService orderInventoryService;
    private final PlatformTransactionManager transactionManager;

    @Value("${app.online-payment-reservation-minutes:30}")
    private long reservationMinutes;

    @Scheduled(fixedDelayString = "${app.order-reservation-cleanup-ms:60000}")
    public void expirePendingOnlinePayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(reservationMinutes);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        while (true) {
            List<HoaDon> candidates = hoaDonRepo.findExpiredPendingOnlinePayments(
                    STATUS_PENDING,
                    now,
                    cutoff,
                    ONLINE_PAYMENT_METHODS,
                    PageRequest.of(0, EXPIRY_BATCH_SIZE)
            );
            if (candidates.isEmpty()) return;

            int completed = 0;
            for (HoaDon candidate : candidates) {
                try {
                    ExpiredOrder expired = transaction.execute(status -> expireOne(candidate.getId(), now));
                    if (expired == null) continue;
                    completed++;
                    emailService.sendOrderStatusUpdateEmail(
                            expired.order(), "Thanh toán thất bại", expired.note());
                } catch (RuntimeException ex) {
                    log.warn("Không thể xử lý đơn thanh toán hết hạn #{}; scheduler sẽ thử lại ở lượt sau",
                            candidate.getId(), ex);
                }
            }

            if (candidates.size() < EXPIRY_BATCH_SIZE || completed == 0) return;
        }
    }

    private ExpiredOrder expireOne(Integer orderId, LocalDateTime now) {
        HoaDon order = hoaDonRepo.findByIdForUpdate(orderId).orElse(null);
        if (order == null
                || order.getTrangThai() == null
                || order.getTrangThai() != STATUS_PENDING
                || Boolean.TRUE.equals(order.getDaThanhToan())) {
            return null;
        }
        orderInventoryService.restoreReservation(order);
        order.setTrangThai(STATUS_PAYMENT_FAILED);
        order.setDaThanhToan(false);
        order.setPhuongThucThanhToanOnline("FAILED");
        order.setTrangThaiTracking("payment_failed");
        order.setUrlThanhToan(null);
        order.setThanhToanHetHan(null);
        hoaDonRepo.save(order);

        String note = "Tự động hoàn kho do thanh toán online quá hạn " + reservationMinutes + " phút.";
        trackingRepo.save(LichSuTracking.builder()
                .hoaDon(order)
                .trangThai("payment_failed")
                .moTa(note)
                .ngayCapNhat(now)
                .build());
        auditLogRepo.save(HoaDonAuditLog.builder()
                .hoaDon(order)
                .hanhDong("HET_HAN_THANH_TOAN")
                .trangThaiCu(STATUS_PENDING)
                .trangThaiMoi(STATUS_PAYMENT_FAILED)
                .nguoiThucHien("System")
                .vaiTro("System")
                .ghiChu(note)
                .ngayTao(now)
                .build());
        return new ExpiredOrder(order, note);
    }

    private record ExpiredOrder(HoaDon order, String note) {
    }
}
