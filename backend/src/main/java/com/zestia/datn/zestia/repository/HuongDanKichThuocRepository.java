package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HuongDanKichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HuongDanKichThuocRepository extends JpaRepository<HuongDanKichThuoc, Integer> {
    List<HuongDanKichThuoc> findBySanPhamIdOrderByKichThuocId(Integer sanPhamId);

    Optional<HuongDanKichThuoc> findBySanPhamIdAndKichThuocId(Integer sanPhamId, Integer kichThuocId);

    void deleteBySanPhamId(Integer sanPhamId);
}
