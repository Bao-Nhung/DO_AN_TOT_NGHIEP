package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HuongDanKichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HuongDanKichThuocRepository extends JpaRepository<HuongDanKichThuoc, Integer> {
    List<HuongDanKichThuoc> findByVayIdOrderByKichThuocId(Integer vayId);

    Optional<HuongDanKichThuoc> findByVayIdAndKichThuocId(Integer vayId, Integer kichThuocId);

    void deleteByVayId(Integer vayId);
}
