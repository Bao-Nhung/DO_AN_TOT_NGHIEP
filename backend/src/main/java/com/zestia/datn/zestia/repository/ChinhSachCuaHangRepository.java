package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.ChinhSachCuaHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChinhSachCuaHangRepository extends JpaRepository<ChinhSachCuaHang, Integer> {
    List<ChinhSachCuaHang> findByTrangThaiOrderByThuTuAsc(Byte trangThai);

    Optional<ChinhSachCuaHang> findByMaChinhSachAndTrangThai(String maChinhSach, Byte trangThai);
}
