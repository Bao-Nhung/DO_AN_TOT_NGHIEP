package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
// 2 DÒNG IMPORT NÀY LÀ ĐỂ SỬA LỖI BẠN VỪA GẶP:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    // Các method cũ của hệ thống
    List<HoaDon> findByKhachHangId(Integer khachHangId);
    List<HoaDon> findByNhanVienId(Integer nhanVienId);
    
    List<HoaDon> findByHinhThucNhanHang(Byte hinhThucNhanHang);
    
    boolean existsByMaHoaDon(String maHoaDon);
    
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    // ==========================================
    // ===== TRACKING QUERIES (MỚI) =====
    // ==========================================

    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon")
    Optional<HoaDon> findOrderByCode(@Param("maHoaDon") String maHoaDon);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.soDienThoai = :soDienThoai ORDER BY h.ngayTao DESC")
    List<HoaDon> findOrdersByPhoneNumber(@Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon AND h.khachHang.soDienThoai = :soDienThoai")
    Optional<HoaDon> findOrderByCodeAndPhone(@Param("maHoaDon") String maHoaDon, @Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiTracking = :trangThaiTracking ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrackingStatus(@Param("trangThaiTracking") String trangThaiTracking);
}