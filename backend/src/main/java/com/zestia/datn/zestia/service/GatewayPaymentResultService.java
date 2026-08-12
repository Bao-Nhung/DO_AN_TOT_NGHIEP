package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.LichSuThanhToan;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GatewayPaymentResultService {
    private static final byte STATUS_CONFIRMED = 1;
    private static final byte STATUS_CANCELLED = 5;
    private static final byte STATUS_PAYMENT_FAILED = 7;

    private final HoaDonRepository hoaDonRepo;
    private final LichSuThanhToanRepository paymentHistoryRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final OrderInventoryService inventoryService;
    private final EmailService emailService;

    @Transactional
    public PaymentOutcome applyById(Integer orderId, String method, BigDecimal amount,
                                    String transactionId, boolean successful) {
        if (orderId == null) return PaymentOutcome.notFound();
        return hoaDonRepo.findByIdForUpdate(orderId)
                .map(order -> applyLocked(order, method, amount, transactionId, successful))
                .orElseGet(PaymentOutcome::notFound);
    }

    @Transactional
    public PaymentOutcome applyByCode(String orderCode, String method, BigDecimal amount,
                                      String transactionId, boolean successful) {
        if (orderCode == null || orderCode.isBlank()) return PaymentOutcome.notFound();
        return hoaDonRepo.findByMaHoaDonForUpdate(orderCode.trim())
                .map(order -> applyLocked(order, method, amount, transactionId, successful))
                .orElseGet(PaymentOutcome::notFound);
    }

    private PaymentOutcome applyLocked(HoaDon order, String method, BigDecimal amount,
                                       String transactionId, boolean successful) {
        String normalizedMethod = normalizeMethod(method);
        String safeTransactionId = normalizeTransactionId(transactionId, normalizedMethod, order.getId());

        String expectedMethod = normalizeMethod(order.getHinhThucThanhToan());
        if (!Set.of("MOMO", "ZALOPAY").contains(normalizedMethod)
                || !expectedMethod.equals(normalizedMethod)) {
            return new PaymentOutcome(false, false, order.getMaHoaDon(), order.getTongTien(),
                    "Cổng thanh toán không khớp với phương thức đã chọn cho đơn hàng");
        }

        String historyStatus = successful ? "SUCCESS" : "FAILED";
        Optional<LichSuThanhToan> existingEvent = paymentHistoryRepo
                .findFirstByMaGiaoDichAndPhuongThucAndTrangThai(
                        safeTransactionId, normalizedMethod, historyStatus);
        if (existingEvent.isPresent()) {
            boolean belongsToOrder = existingEvent.get().getHoaDon() != null
                    && existingEvent.get().getHoaDon().getId().equals(order.getId());
            if (!belongsToOrder) {
                return new PaymentOutcome(false, false, order.getMaHoaDon(), order.getTongTien(),
                        "Mã giao dịch đã được ghi nhận cho một đơn hàng khác");
            }
            return new PaymentOutcome(Boolean.TRUE.equals(order.getDaThanhToan()), true,
                    order.getMaHoaDon(), order.getTongTien(), "Kết quả thanh toán đã được xử lý");
        }

        if (Boolean.TRUE.equals(order.getDaThanhToan())) {
            return new PaymentOutcome(true, true, order.getMaHoaDon(), order.getTongTien(), "Đơn hàng đã được thanh toán");
        }

        if (successful && !amountMatches(amount, order.getTongTien())) {
            if (paymentHistoryRepo.existsByMaGiaoDichAndPhuongThucAndTrangThai(
                    safeTransactionId, normalizedMethod, "FAILED")) {
                return new PaymentOutcome(false, true, order.getMaHoaDon(), order.getTongTien(),
                        "Kết quả thanh toán sai số tiền đã được xử lý");
            }
            return failLocked(order, normalizedMethod, amount, safeTransactionId,
                    "Số tiền cổng thanh toán trả về không khớp tổng tiền đơn hàng");
        }
        boolean terminal = order.getTrangThai() != null
                && (order.getTrangThai() == STATUS_CANCELLED || order.getTrangThai() == STATUS_PAYMENT_FAILED);
        if (successful && (terminal || Boolean.TRUE.equals(order.getDaHoanTonKho()))) {
            if (!paymentHistoryRepo.existsByMaGiaoDichAndPhuongThucAndTrangThai(
                    safeTransactionId, normalizedMethod, "LATE_SUCCESS")) {
                paymentHistoryRepo.save(LichSuThanhToan.builder()
                        .hoaDon(order)
                        .soTien(Optional.ofNullable(amount).orElse(BigDecimal.ZERO))
                        .phuongThuc(normalizedMethod)
                        .maGiaoDich(safeTransactionId)
                        .trangThai("LATE_SUCCESS")
                        .noiDung("Thanh toán đến muộn, cần đối soát - " + order.getMaHoaDon())
                        .ngayTao(LocalDateTime.now())
                        .build());
            }
            saveAudit(order, "THANH_TOAN_DEN_MUON", order.getTrangThai(), order.getTrangThai(),
                    "Cổng " + normalizedMethod + " báo thành công sau khi đơn đã hoàn tồn kho");
            return new PaymentOutcome(false, false, order.getMaHoaDon(), order.getTongTien(),
                    "Giao dịch đến muộn, cần nhân viên đối soát");
        }
        if (terminal) {
            return new PaymentOutcome(false, true, order.getMaHoaDon(), order.getTongTien(),
                    "Đơn hàng đã ở trạng thái kết thúc");
        }
        if (successful && order.getTrangThai() != null && order.getTrangThai() != 0) {
            return new PaymentOutcome(false, false, order.getMaHoaDon(), order.getTongTien(),
                    "Đơn hàng không còn ở trạng thái chờ thanh toán");
        }

        if (successful) {
            byte oldStatus = order.getTrangThai() != null ? order.getTrangThai() : 0;
            order.setTrangThai(STATUS_CONFIRMED);
            order.setTrangThaiTracking("confirmed");
            order.setDaThanhToan(true);
            order.setPhuongThucThanhToanOnline(normalizedMethod);
            order.setMaGiaoDichCong(safeTransactionId);
            order.setUrlThanhToan(null);
            order.setThanhToanHetHan(null);
            hoaDonRepo.save(order);

            paymentHistoryRepo.save(LichSuThanhToan.builder()
                    .hoaDon(order)
                    .soTien(amount)
                    .phuongThuc(normalizedMethod)
                    .maGiaoDich(safeTransactionId)
                    .trangThai("SUCCESS")
                    .noiDung("Thanh toán " + normalizedMethod + " thành công - " + order.getMaHoaDon())
                    .ngayTao(LocalDateTime.now())
                    .build());
            saveTracking(order, "confirmed", "Thanh toán " + normalizedMethod + " thành công. Đơn hàng đã được xác nhận.");
            saveAudit(order, "THANH_TOAN_THANH_CONG", oldStatus, STATUS_CONFIRMED,
                    "Mã giao dịch: " + safeTransactionId);
            afterCommit(() -> emailService.sendOrderConfirmationEmail(order));
            return new PaymentOutcome(true, false, order.getMaHoaDon(), order.getTongTien(), "Thanh toán thành công");
        }

        return failLocked(order, normalizedMethod, amount, safeTransactionId,
                "Cổng thanh toán xác nhận giao dịch thất bại");
    }

    private PaymentOutcome failLocked(HoaDon order, String method, BigDecimal amount,
                                      String transactionId, String reason) {
        byte oldStatus = order.getTrangThai() != null ? order.getTrangThai() : 0;
        inventoryService.restoreReservation(order);
        order.setDaThanhToan(false);
        order.setPhuongThucThanhToanOnline("FAILED");
        order.setMaGiaoDichCong(transactionId);
        order.setUrlThanhToan(null);
        order.setThanhToanHetHan(null);
        order.setTrangThai(STATUS_PAYMENT_FAILED);
        order.setTrangThaiTracking("payment_failed");
        hoaDonRepo.save(order);

        if (!paymentHistoryRepo.existsByMaGiaoDichAndPhuongThucAndTrangThai(transactionId, method, "FAILED")) {
            paymentHistoryRepo.save(LichSuThanhToan.builder()
                    .hoaDon(order)
                    .soTien(Optional.ofNullable(amount).orElse(BigDecimal.ZERO))
                    .phuongThuc(method)
                    .maGiaoDich(transactionId)
                    .trangThai("FAILED")
                    .noiDung(reason + " - " + order.getMaHoaDon())
                    .ngayTao(LocalDateTime.now())
                    .build());
        }
        saveTracking(order, "payment_failed", reason + ". Đơn hàng không được xử lý tiếp.");
        saveAudit(order, "THANH_TOAN_THAT_BAI", oldStatus, STATUS_PAYMENT_FAILED,
                reason + ". Mã giao dịch: " + transactionId);
        afterCommit(() -> emailService.sendOrderStatusUpdateEmail(order, "Thanh toán thất bại", reason));
        return new PaymentOutcome(false, false, order.getMaHoaDon(), order.getTongTien(), reason);
    }

    private void saveTracking(HoaDon order, String status, String description) {
        trackingRepo.save(LichSuTracking.builder()
                .hoaDon(order)
                .trangThai(status)
                .moTa(description)
                .ngayCapNhat(LocalDateTime.now())
                .build());
    }

    private void saveAudit(HoaDon order, String action, Byte oldStatus, Byte newStatus, String note) {
        auditLogRepo.save(HoaDonAuditLog.builder()
                .hoaDon(order)
                .hanhDong(action)
                .trangThaiCu(oldStatus)
                .trangThaiMoi(newStatus)
                .nguoiThucHien("Payment Gateway")
                .vaiTro("System")
                .ghiChu(note)
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private boolean amountMatches(BigDecimal actual, BigDecimal expected) {
        if (actual == null || expected == null) return false;
        return actual.setScale(0, RoundingMode.HALF_UP)
                .compareTo(expected.setScale(0, RoundingMode.HALF_UP)) == 0;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) return "ONLINE";
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeTransactionId(String transactionId, String method, Integer orderId) {
        if (transactionId != null && !transactionId.isBlank()) return transactionId.trim();
        return method + "-ORDER-" + orderId;
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    public record PaymentOutcome(boolean success, boolean idempotent, String orderCode,
                                 BigDecimal amount, String message) {
        public static PaymentOutcome notFound() {
            return new PaymentOutcome(false, false, "", BigDecimal.ZERO, "Không tìm thấy đơn hàng");
        }
    }
}
