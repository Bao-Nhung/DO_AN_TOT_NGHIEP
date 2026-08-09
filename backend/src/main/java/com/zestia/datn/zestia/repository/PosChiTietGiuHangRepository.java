package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.PosChiTietGiuHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosChiTietGiuHangRepository extends JpaRepository<PosChiTietGiuHang, Integer> {

    List<PosChiTietGiuHang> findByPhienIdOrderById(Integer phienId);

    Optional<PosChiTietGiuHang> findByPhienIdAndSanPhamChiTietId(Integer phienId, Integer sanPhamChiTietId);
}
