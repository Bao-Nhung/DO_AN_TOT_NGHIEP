package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangRepository
        extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);
}