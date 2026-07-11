package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderReservationService {

    private static final byte STATUS_PENDING = 0;
    private static final byte STATUS_PAYMENT_FAILED = 7;
    private static final List<String> ONLINE_PAYMENT_METHODS = List.of("MOMO", "ZALOPAY");

    private final HoaDonRepository hoaDonRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final EmailService emailService;
    private final OrderInventoryService orderInventoryService;

    @Value("${app.online-payment-reservation-minutes:30}")
    private long reservationMinutes;

    @Scheduled(fixedDelayString = "${app.order-reservation-cleanup-ms:60000}")
    @Transactional
    public void expirePendingOnlinePayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(reservationMinutes);
        List<HoaDon> expiredOrders = hoaDonRepo.findExpiredPendingOnlinePayments(
                STATUS_PENDING,
                cutoff,
                ONLINE_PAYMENT_METHODS
        );

        for (HoaDon order : expiredOrders) {
            orderInventoryService.restoreReservation(order);
            order.setTrangThai(STATUS_PAYMENT_FAILED);
            order.setDaThanhToan(false);
            order.setPhuongThucThanhToanOnline("FAILED");
            order.setTrangThaiTracking("payment_failed");
            hoaDonRepo.save(order);

            String note = "Tự động hoàn kho do thanh toán online quá hạn " + reservationMinutes + " phút.";
            trackingRepo.save(LichSuTracking.builder()
                    .hoaDon(order)
                    .trangThai("payment_failed")
                    .moTa(note)
                    .ngayCapNhat(LocalDateTime.now())
                    .build());
            auditLogRepo.save(HoaDonAuditLog.builder()
                    .hoaDon(order)
                    .hanhDong("HET_HAN_THANH_TOAN")
                    .trangThaiCu(STATUS_PENDING)
                    .trangThaiMoi(STATUS_PAYMENT_FAILED)
                    .nguoiThucHien("System")
                    .vaiTro("System")
                    .ghiChu(note)
                    .ngayTao(LocalDateTime.now())
                    .build());
            emailService.sendOrderStatusUpdateEmail(order, "Thanh toán thất bại", note);
        }
    }
}
