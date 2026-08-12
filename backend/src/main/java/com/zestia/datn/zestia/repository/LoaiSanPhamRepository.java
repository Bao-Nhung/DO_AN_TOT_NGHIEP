package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LoaiSanPham;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiSanPhamRepository extends JpaRepository<LoaiSanPham, Integer> {
    boolean existsByTenLoaiSanPhamIgnoreCase(String name);
    boolean existsByTenLoaiSanPhamIgnoreCaseAndIdNot(String name, Integer id);
}
