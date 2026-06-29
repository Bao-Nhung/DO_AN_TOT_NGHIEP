package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    List<HoaDon> findByKhachHangId(Integer khachHangId);

    List<HoaDon> findByNhanVienId(Integer nhanVienId);

    List<HoaDon> findByTrangThai(Byte trangThai);

    List<HoaDon> findByHinhThucNhanHang(Byte hinhThucNhanHang);

    boolean existsByMaHoaDon(String maHoaDon);

    // ===== TRACKING QUERIES (MỚI) =====

    /**
     * Tìm đơn hàng theo mã đơn (không cần đăng nhập)
     * @param maHoaDon Mã đơn hàng
     * @return Đơn hàng nếu tìm thấy
     */
    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon")
    Optional<HoaDon> findOrderByCode(@Param("maHoaDon") String maHoaDon);

    /**
     * Tìm đơn hàng theo số điện thoại khách hàng (không cần đăng nhập)
     * @param soDienThoai Số điện thoại khách hàng
     * @return Danh sách đơn hàng của khách hàng
     */
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.soDienThoai = :soDienThoai ORDER BY h.ngayTao DESC")
    List<HoaDon> findOrdersByPhoneNumber(@Param("soDienThoai") String soDienThoai);

    /**
     * Lấy danh sách đơn hàng của khách hàng (sắp xếp mới nhất trước)
     * @param khachHangId ID khách hàng
     * @return Danh sách đơn hàng
     */
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    /**
     * Tìm đơn hàng theo mã và số điện thoại (xác minh)
     * @param maHoaDon Mã đơn hàng
     * @param soDienThoai Số điện thoại khách hàng
     * @return Đơn hàng nếu tìm thấy
     */
    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon = :maHoaDon AND h.khachHang.soDienThoai = :soDienThoai")
    Optional<HoaDon> findOrderByCodeAndPhone(@Param("maHoaDon") String maHoaDon, @Param("soDienThoai") String soDienThoai);

    /**
     * Lấy danh sách đơn hàng theo trạng thái tracking
     * @param trangThaiTracking Trạng thái (pending, processing, shipped, delivered, cancelled)
     * @return Danh sách đơn hàng
     */
    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiTracking = :trangThaiTracking ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrackingStatus(@Param("trangThaiTracking") String trangThaiTracking);

    // ===== END TRACKING QUERIES =====
}