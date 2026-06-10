package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    List<HoaDon> findByKhachHangId(Integer khachHangId);

    List<HoaDon> findByNhanVienId(Integer nhanVienId);

    List<HoaDon> findByTrangThai(Byte trangThai);

    List<HoaDon> findByHinhThucNhanHang(Byte hinhThucNhanHang);

    boolean existsByMaHoaDon(String maHoaDon);
}