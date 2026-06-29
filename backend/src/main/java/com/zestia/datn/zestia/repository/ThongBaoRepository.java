package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    /**
     * Lấy tất cả thông báo của khách hàng
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo (sắp xếp mới nhất trước)
     */
    @Query("SELECT t FROM ThongBao t WHERE t.khachHang.id = :khachHangId ORDER BY t.ngayTao DESC")
    List<ThongBao> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    /**
     * Lấy thông báo chưa đọc của khách hàng
     * @param khachHangId ID khách hàng
     * @return Danh sách thông báo chưa đọc
     */
    @Query("SELECT t FROM ThongBao t WHERE t.khachHang.id = :khachHangId AND t.daDoc = 0 ORDER BY t.ngayTao DESC")
    List<ThongBao> findUnreadNotificationsByCustomerId(@Param("khachHangId") Integer khachHangId);

    /**
     * Lấy thông báo của một đơn hàng
     * @param hoaDonId ID hóa đơn
     * @return Danh sách thông báo
     */
    List<ThongBao> findByHoaDonId(Integer hoaDonId);

    /**
     * Lấy thông báo theo loại
     * @param loaiThongBao Loại thông báo
     * @return Danh sách thông báo
     */
    List<ThongBao> findByLoaiThongBao(String loaiThongBao);
}