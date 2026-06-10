package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NhanVienRepository
        extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByTenNguoiDung(String tenNguoiDung);

    boolean existsByEmail(String email);

    boolean existsByTenNguoiDung(String tenNguoiDung);
}