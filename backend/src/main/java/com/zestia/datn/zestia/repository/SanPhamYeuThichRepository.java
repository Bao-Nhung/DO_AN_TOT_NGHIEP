package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SanPhamYeuThich;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SanPhamYeuThichRepository extends JpaRepository<SanPhamYeuThich, Integer> {
    List<SanPhamYeuThich> findByKhachHangIdOrderByNgayTaoDesc(Integer khachHangId);

    Optional<SanPhamYeuThich> findByKhachHangIdAndSanPhamId(Integer khachHangId, Integer sanPhamId);

    void deleteByKhachHangIdAndSanPhamId(Integer khachHangId, Integer sanPhamId);
}
