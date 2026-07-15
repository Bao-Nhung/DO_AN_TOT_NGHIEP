package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {

    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);

    Optional<LichSuThanhToan> findByMaGiaoDich(String maGiaoDich);

    boolean existsByMaGiaoDichAndPhuongThucAndTrangThai(String maGiaoDich, String phuongThuc, String trangThai);
}
