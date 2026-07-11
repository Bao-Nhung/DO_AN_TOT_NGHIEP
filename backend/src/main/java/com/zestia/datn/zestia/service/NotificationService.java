package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.ThongBao;
import com.zestia.datn.zestia.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ThongBaoRepository thongBaoRepo;

    /**
     * Tạo thông báo mới (hệ thống, voucher, v.v.)
     * @param tieuDe Tiêu đề thông báo
     * @param noiDung Nội dung chi tiết
     * @param loai Loại thông báo (HeThong, Voucher, DonHang, etc.)
     * @param trangThai Trạng thái (1: Hiển thị, 0: Nháp)
     * @return Đối tượng ThongBao đã lưu
     */
    public ThongBao createNotification(String tieuDe, String noiDung, String loai, Byte trangThai) {
        try {
            ThongBao thongBao = ThongBao.builder()
                    .tieuDe(tieuDe)
                    .noiDung(noiDung)
                    .loai(loai)
                    .trangThai(trangThai != null ? trangThai : (byte) 1)
                    .ngayTao(LocalDateTime.now())
                    .build();
            ThongBao saved = thongBaoRepo.save(thongBao);
            log.info("Đã tạo thông báo mới thành công: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo: ", e);
            return null;
        }
    }

    /**
     * Lấy tất cả thông báo đang hoạt động (trang_thai = 1)
     * @return Danh sách thông báo
     */
    public List<ThongBao> getActiveNotifications() {
        try {
            return thongBaoRepo.findByTrangThaiOrderByNgayTaoDesc((byte) 1);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thông báo hoạt động: ", e);
            return List.of();
        }
    }

    /**
     * Lấy tất cả thông báo (dành cho Admin quản lý)
     * @return Danh sách thông báo
     */
    public List<ThongBao> getAllNotifications() {
        try {
            return thongBaoRepo.findAllByOrderByNgayTaoDesc();
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả thông báo: ", e);
            return List.of();
        }
    }

    /**
     * Xóa thông báo theo ID
     * @param id ID thông báo
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteNotification(Integer id) {
        try {
            if (thongBaoRepo.existsById(id)) {
                thongBaoRepo.deleteById(id);
                log.info("Đã xóa thông báo ID: {}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi xóa thông báo ID " + id + ": ", e);
            return false;
        }
    }
}
