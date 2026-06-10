package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    List<HoaDonChiTiet> findByVayChiTietId(Integer vayChiTietId);

    void deleteByHoaDonId(Integer hoaDonId);

    long countByHoaDonId(Integer hoaDonId);
}