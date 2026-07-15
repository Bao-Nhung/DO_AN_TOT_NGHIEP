package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.repository.HoaDonAuditLogRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
    private static final byte STATUS_CANCELLED = 5;
    private static final byte STATUS_DELIVERY_FAILED = 6;
    private static final byte STATUS_REFUNDED = 9;
    private static final Map<Byte, Set<Byte>> ALLOWED_TRANSITIONS = Map.of(
            (byte) 0, Set.of((byte) 1, STATUS_CANCELLED),
            (byte) 1, Set.of((byte) 2, STATUS_CANCELLED),
            (byte) 2, Set.of((byte) 3, STATUS_CANCELLED),
            (byte) 3, Set.of((byte) 4, STATUS_DELIVERY_FAILED),
            (byte) 4, Set.of((byte) 8),
            (byte) 8, Set.of((byte) 4, STATUS_REFUNDED)
    );

    private final HoaDonRepository hoaDonRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final HoaDonAuditLogRepository auditLogRepo;
    private final OrderInventoryService inventoryService;
    private final EmailService emailService;

    @Transactional
    public HoaDon transition(Integer orderId, byte newStatus, String note, String actorName, String actorRole) {
        HoaDon order = hoaDonRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        byte oldStatus = order.getTrangThai() != null ? order.getTrangThai() : 0;
        if (oldStatus == newStatus) return order;

        if (!ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of()).contains(newStatus)) {
            throw new IllegalStateException("Không thể chuyển đơn từ '" + label(oldStatus) + "' sang '" + label(newStatus) + "'");
        }
        if (newStatus == 1 && isOnline(order) && !Boolean.TRUE.equals(order.getDaThanhToan())) {
            throw new IllegalStateException("Đơn MoMo/ZaloPay chỉ được xác nhận sau khi cổng thanh toán báo thành công");
        }

        String trackingStatus = trackingStatus(newStatus);
        String description = note != null && !note.isBlank() ? note.trim() : defaultDescription(newStatus);
        order.setTrangThai(newStatus);
        order.setTrangThaiTracking(trackingStatus);
        if (newStatus == 4) {
            order.setNgayGiaoHangThucTe(LocalDateTime.now());
            if ("COD".equalsIgnoreCase(order.getHinhThucThanhToan())) order.setDaThanhToan(true);
        }
        if (newStatus == STATUS_CANCELLED || newStatus == STATUS_DELIVERY_FAILED || newStatus == STATUS_REFUNDED) {
            inventoryService.restoreReservation(order);
        }
        if (note != null && !note.isBlank()) order.setGhiChu(note.trim());
        hoaDonRepo.save(order);

        trackingRepo.save(LichSuTracking.builder()
                .hoaDon(order)
                .trangThai(trackingStatus)
                .moTa(description)
                .ngayCapNhat(LocalDateTime.now())
                .build());
        auditLogRepo.save(HoaDonAuditLog.builder()
                .hoaDon(order)
                .hanhDong("CAP_NHAT_TRANG_THAI")
                .trangThaiCu(oldStatus)
                .trangThaiMoi(newStatus)
                .nguoiThucHien(actorName != null ? actorName : "System")
                .vaiTro(actorRole != null ? actorRole : "System")
                .ghiChu(description)
                .ngayTao(LocalDateTime.now())
                .build());

        afterCommit(() -> sendStatusEmail(order, newStatus, description));
        return order;
    }

    private boolean isOnline(HoaDon order) {
        String method = order.getHinhThucThanhToan();
        return "MOMO".equalsIgnoreCase(method) || "ZALOPAY".equalsIgnoreCase(method);
    }

    private void sendStatusEmail(HoaDon order, byte status, String description) {
        if (status == 4) {
            emailService.sendDeliveryConfirmationEmail(order);
        } else if (status == STATUS_CANCELLED) {
            emailService.sendOrderCancellationEmail(order, description);
        } else {
            emailService.sendOrderStatusUpdateEmail(order, label(status), description);
        }
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

    public static String trackingStatus(byte status) {
        return switch (status) {
            case 0 -> "pending";
            case 1 -> "confirmed";
            case 2 -> "processing";
            case 3 -> "shipped";
            case 4 -> "delivered";
            case 5 -> "cancelled";
            case 6 -> "failed";
            case 7 -> "payment_failed";
            case 8 -> "return_requested";
            case 9 -> "refunded";
            default -> "pending";
        };
    }

    public static String label(byte status) {
        return switch (status) {
            case 0 -> "Chờ xử lý";
            case 1 -> "Đã xác nhận";
            case 2 -> "Đang chuẩn bị";
            case 3 -> "Đang giao hàng";
            case 4 -> "Giao hàng thành công";
            case 5 -> "Đã hủy";
            case 6 -> "Giao hàng thất bại";
            case 7 -> "Thanh toán thất bại";
            case 8 -> "Yêu cầu đổi/trả";
            case 9 -> "Đã hoàn tiền/hoàn tất";
            default -> "Không xác định";
        };
    }

    private String defaultDescription(byte status) {
        return switch (status) {
            case 1 -> "Đơn hàng đã được xác nhận.";
            case 2 -> "Đơn hàng đang được chuẩn bị và đóng gói.";
            case 3 -> "Đơn hàng đã được bàn giao cho đơn vị vận chuyển.";
            case 4 -> "Giao hàng thành công đến tay người nhận.";
            case 5 -> "Đơn hàng đã bị hủy.";
            case 6 -> "Giao hàng thất bại. Sản phẩm và lượt voucher đã được hoàn lại.";
            case 8 -> "Khách hàng yêu cầu đổi/trả hàng.";
            case 9 -> "Đã xử lý đổi/trả và hoàn tiền.";
            default -> "Trạng thái đơn hàng đã được cập nhật.";
        };
    }
}
