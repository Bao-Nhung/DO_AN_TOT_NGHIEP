package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichSuXem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LichSuXemRepository extends JpaRepository<LichSuXem, Integer> {
    List<LichSuXem> findTop20ByKhachHangIdOrderByNgayXemDesc(Integer khachHangId);

    Optional<LichSuXem> findByKhachHangIdAndSanPhamId(Integer khachHangId, Integer sanPhamId);
}
