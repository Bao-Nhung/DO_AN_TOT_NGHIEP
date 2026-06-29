package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichSuTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LichSuTrackingRepository extends JpaRepository<LichSuTracking, Integer> {

    /**
     * Lấy lịch sử tracking của một đơn hàng
     * @param hoaDonId ID hóa đơn
     * @return Danh sách lịch sử tracking (sắp xếp mới nhất trước)
     */
    @Query("SELECT l FROM LichSuTracking l WHERE l.hoaDon.id = :hoaDonId ORDER BY l.ngayCapNhat DESC")
    List<LichSuTracking> findByHoaDonIdOrderByLatest(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Lấy trạng thái tracking mới nhất của đơn hàng
     * @param hoaDonId ID hóa đơn
     * @return Lịch sử tracking mới nhất
     */
    @Query("SELECT l FROM LichSuTracking l WHERE l.hoaDon.id = :hoaDonId ORDER BY l.ngayCapNhat DESC LIMIT 1")
    LichSuTracking findLatestTrackingByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    List<LichSuTracking> findByHoaDonId(Integer hoaDonId);
}