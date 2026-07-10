package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
// 2 DÒNG IMPORT NÀY LÀ ĐỂ SỬA LỖI BẠN VỪA GẶP:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

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

    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE kh.soDienThoai = :soDienThoai OR h.soDienThoai = :soDienThoai ORDER BY h.ngayTao DESC")
    List<HoaDon> findOrdersByPhoneNumber(@Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE h.maHoaDon = :maHoaDon AND (kh.soDienThoai = :soDienThoai OR h.soDienThoai = :soDienThoai)")
    Optional<HoaDon> findOrderByCodeAndPhone(@Param("maHoaDon") String maHoaDon, @Param("soDienThoai") String soDienThoai);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiTracking = :trangThaiTracking ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrackingStatus(@Param("trangThaiTracking") String trangThaiTracking);

    @Query("""
            SELECT h FROM HoaDon h
            WHERE h.trangThai = :status
              AND (h.daThanhToan IS NULL OR h.daThanhToan = false)
              AND h.ngayTao < :cutoff
              AND UPPER(h.hinhThucThanhToan) IN :onlineMethods
              AND (h.phuongThucThanhToanOnline IS NULL OR UPPER(h.phuongThucThanhToanOnline) <> 'FAILED')
            """)
    List<HoaDon> findExpiredPendingOnlinePayments(@Param("status") Byte status,
                                                  @Param("cutoff") LocalDateTime cutoff,
                                                  @Param("onlineMethods") List<String> onlineMethods);
}
