package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {

    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);

    Optional<LichSuThanhToan> findFirstByHoaDonIdAndTrangThaiOrderByNgayTaoDesc(
            Integer hoaDonId, String trangThai);

    Optional<LichSuThanhToan> findByMaGiaoDich(String maGiaoDich);

    boolean existsByMaGiaoDichAndPhuongThucAndTrangThai(String maGiaoDich, String phuongThuc, String trangThai);

    boolean existsByHoaDonIdAndPhuongThucAndTrangThai(Integer hoaDonId, String phuongThuc, String trangThai);

    Optional<LichSuThanhToan> findFirstByMaGiaoDichAndPhuongThucAndTrangThai(
            String maGiaoDich, String phuongThuc, String trangThai);

    @Query("SELECT COALESCE(SUM(l.soTien), 0) FROM LichSuThanhToan l WHERE l.trangThai = 'REFUND_SUCCESS'")
    BigDecimal sumSuccessfulRefundAdjustments();

    @Query("""
            SELECT COALESCE(SUM(l.soTien), 0) FROM LichSuThanhToan l
            WHERE l.trangThai = 'REFUND_SUCCESS'
              AND l.hoaDon.trangThai = 4
              AND l.hoaDon.khachHang.id = :customerId
            """)
    BigDecimal sumRefundAdjustmentsForCompletedOrders(@Param("customerId") Integer customerId);

    @Query("""
            SELECT COALESCE(SUM(l.soTien), 0) FROM LichSuThanhToan l
            WHERE l.trangThai = 'REFUND_SUCCESS'
              AND l.hoaDon.id = :orderId
            """)
    BigDecimal sumRefundAdjustmentsByOrderId(@Param("orderId") Integer orderId);
}
