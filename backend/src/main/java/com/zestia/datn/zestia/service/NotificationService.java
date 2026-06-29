package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ThongBaoRepository thongBaoRepo;

    /**
     * Tạo thông báo đơn hàng được tạo thành công
     * @param hoaDon Đơn hàng vừa tạo
     */
    public void createOrderCreatedNotification(HoaDon hoaDon) {
        try {
            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(hoaDon)
                    .khachHang(khachHang)
                    .loaiThongBao("order_created")
                    .tieuDe("Đơn hàng #" + hoaDon.getMaHoaDon() + " đã được tạo thành công")
                    .noiDung("Cảm ơn bạn đã đặt hàng. Chúng tôi sẽ xử lý đơn hàng của bạn soonest. " +
                            "Tổng tiền: " + hoaDon.getTongTien() + "đ")
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo đơn hàng được tạo cho khách hàng: {}", khachHang.getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo đơn hàng được tạo: ", e);
        }
    }

    /**
     * Tạo thông báo sản phẩm đã được thêm vào giỏ
     * @param khachHang Khách hàng
     * @param tenSanPham Tên sản phẩm
     */
    public void createAddToCartNotification(KhachHang khachHang, String tenSanPham) {
        try {
            if (khachHang == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(null)
                    .khachHang(khachHang)
                    .loaiThongBao("add_to_cart")
                    .tieuDe("Sản phẩm đã được thêm vào giỏ")
                    .noiDung("'" + tenSanPham + "' đã được thêm vào giỏ hàng của bạn.")
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo thêm vào giỏ cho khách hàng: {}", khachHang.getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo thêm vào giỏ: ", e);
        }
    }

    /**
     * Tạo thông báo đơn hàng đang được xử lý
     * @param hoaDon Đơn hàng
     */
    public void createOrderProcessingNotification(HoaDon hoaDon) {
        try {
            if (hoaDon.getKhachHang() == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(hoaDon)
                    .khachHang(hoaDon.getKhachHang())
                    .loaiThongBao("order_processing")
                    .tieuDe("Đơn hàng #" + hoaDon.getMaHoaDon() + " đang được xử lý")
                    .noiDung("Đơn hàng của bạn đang được chuẩn bị để giao hàng.")
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo đơn hàng đang xử lý cho khách hàng: {}", hoaDon.getKhachHang().getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo đơn hàng đang xử lý: ", e);
        }
    }

    /**
     * Tạo thông báo đơn hàng đã được gửi
     * @param hoaDon Đơn hàng
     */
    public void createOrderShippedNotification(HoaDon hoaDon) {
        try {
            if (hoaDon.getKhachHang() == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(hoaDon)
                    .khachHang(hoaDon.getKhachHang())
                    .loaiThongBao("order_shipped")
                    .tieuDe("Đơn hàng #" + hoaDon.getMaHoaDon() + " đã được gửi")
                    .noiDung("Đơn hàng của bạn đã được gửi đi. " +
                            "Địa chỉ giao: " + hoaDon.getDiaChiGiaoHang())
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo đơn hàng đã gửi cho khách hàng: {}", hoaDon.getKhachHang().getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo đơn hàng đã gửi: ", e);
        }
    }

    /**
     * Tạo thông báo đơn hàng đã giao
     * @param hoaDon Đơn hàng
     */
    public void createOrderDeliveredNotification(HoaDon hoaDon) {
        try {
            if (hoaDon.getKhachHang() == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(hoaDon)
                    .khachHang(hoaDon.getKhachHang())
                    .loaiThongBao("order_delivered")
                    .tieuDe("✓ Đơn hàng #" + hoaDon.getMaHoaDon() + " đã giao thành công")
                    .noiDung("Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua hàng tại ZESTIA!")
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo đơn hàng đã giao cho khách hàng: {}", hoaDon.getKhachHang().getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo đơn hàng đã giao: ", e);
        }
    }

    /**
     * Tạo thông báo đơn hàng bị hủy
     * @param hoaDon Đơn hàng
     * @param lyDo Lý do hủy
     */
    public void createOrderCancelledNotification(HoaDon hoaDon, String lyDo) {
        try {
            if (hoaDon.getKhachHang() == null) {
                log.warn("Không thể tạo thông báo - khách hàng null");
                return;
            }

            ThongBao thongBao = ThongBao.builder()
                    .hoaDon(hoaDon)
                    .khachHang(hoaDon.getKhachHang())
                    .loaiThongBao("order_cancelled")
                    .tieuDe("Đơn hàng #" + hoaDon.getMaHoaDon() + " đã bị hủy")
                    .noiDung("Đơn hàng của bạn đã bị hủy. " +
                            (lyDo != null ? "Lý do: " + lyDo : ""))
                    .daDoc((byte) 0)
                    .ngayTao(LocalDateTime.now())
                    .build();

            thongBaoRepo.save(thongBao);
            log.info("Tạo thông báo đơn hàng bị hủy cho khách hàng: {}", hoaDon.getKhachHang().getId());
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo đơn hàng bị hủy: ", e);
        }
    }

    /**
     * Tạo thông báo không tìm thấy đơn hàng
     * @param maHoaDon Mã đơn hàng
     */
    public void createOrderNotFoundNotification(String maHoaDon) {
        try {
            log.warn("Không tìm thấy đơn hàng: {}", maHoaDon);
            // Có thể lưu vào logs hoặc database để admin xem
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo không tìm thấy đơn: ", e);
        }
    }

    /**
     * Lấy danh sách thông báo của khách hàng
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo
     */
    public List<ThongBao> getNotificationsByCustomerId(Integer khachHangId) {
        try {
            return thongBaoRepo.findByCustomerIdOrderByLatest(khachHangId);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông báo: ", e);
            return List.of();
        }
    }

    /**
     * Lấy danh sách thông báo chưa đọc
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo chưa đọc
     */
    public List<ThongBao> getUnreadNotifications(Integer khachHangId) {
        try {
            return thongBaoRepo.findUnreadNotificationsByCustomerId(khachHangId);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông báo chưa đọc: ", e);
            return List.of();
        }
    }

    /**
     * Đánh dấu thông báo đã đọc
     * @param thongBaoId ID thông báo
     */
    public void markAsRead(Integer thongBaoId) {
        try {
            thongBaoRepo.findById(thongBaoId).ifPresent(tb -> {
                tb.setDaDoc((byte) 1);
                thongBaoRepo.save(tb);
            });
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu thông báo đã đọc: ", e);
        }
    }

    /**
     * Đánh dấu tất cả thông báo của khách hàng đã đọc
     * @param khachHangId ID khách hàng
     */
    public void markAllAsRead(Integer khachHangId) {
        try {
            List<ThongBao> unreadNotifications = getUnreadNotifications(khachHangId);
            unreadNotifications.forEach(tb -> {
                tb.setDaDoc((byte) 1);
                thongBaoRepo.save(tb);
            });
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tất cả thông báo đã đọc: ", e);
        }
    }

    /**
     * Xóa thông báo
     * @param thongBaoId ID thông báo
     */
    public void deleteNotification(Integer thongBaoId) {
        try {
            thongBaoRepo.deleteById(thongBaoId);
            log.info("Xóa thông báo: {}", thongBaoId);
        } catch (Exception e) {
            log.error("Lỗi khi xóa thông báo: ", e);
        }
    }

    /**
     * Lấy số lượng thông báo chưa đọc
     * @param khachHangId ID khách hàng
     * @return Số lượng thông báo chưa đọc
     */
    public long getUnreadCount(Integer khachHangId) {
        try {
            return getUnreadNotifications(khachHangId).size();
        } catch (Exception e) {
            log.error("Lỗi khi lấy số thông báo chưa đọc: ", e);
            return 0;
        }
    }
}