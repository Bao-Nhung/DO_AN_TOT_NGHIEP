package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
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
    private static final List<String> ONLINE_PAYMENT_METHODS = List.of("MOMO", "ZALOPAY", "VNPAY");

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final VayChiTietRepository vayCtRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final EmailService emailService;

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
            restoreStock(order);
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

    private void restoreStock(HoaDon order) {
        List<HoaDonChiTiet> details = hoaDonCtRepo.findByHoaDonId(order.getId());
        for (HoaDonChiTiet detail : details) {
            VayChiTiet variant = detail.getVayChiTiet();
            if (variant == null || detail.getSoLuong() == null) continue;
            int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            variant.setSoLuong(currentStock + detail.getSoLuong());
            vayCtRepo.save(variant);
        }
    }
}
