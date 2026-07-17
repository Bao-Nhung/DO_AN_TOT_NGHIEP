package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {

    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);

    Optional<LichSuThanhToan> findFirstByHoaDonIdAndTrangThaiOrderByNgayTaoDesc(
            Integer hoaDonId, String trangThai);

    Optional<LichSuThanhToan> findByMaGiaoDich(String maGiaoDich);

    boolean existsByMaGiaoDichAndPhuongThucAndTrangThai(String maGiaoDich, String phuongThuc, String trangThai);

    @Query("SELECT COALESCE(SUM(l.soTien), 0) FROM LichSuThanhToan l WHERE l.trangThai = 'REFUND_SUCCESS'")
    BigDecimal sumSuccessfulRefunds();
}
