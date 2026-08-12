package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SanPhamYeuThich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface SanPhamYeuThichRepository extends JpaRepository<SanPhamYeuThich, Integer> {
    @EntityGraph(attributePaths = "sanPham")
    List<SanPhamYeuThich> findByKhachHangIdOrderByNgayTaoDesc(Integer khachHangId);

    Optional<SanPhamYeuThich> findByKhachHangIdAndSanPhamId(Integer khachHangId, Integer sanPhamId);

    void deleteByKhachHangIdAndSanPhamId(Integer khachHangId, Integer sanPhamId);
}
