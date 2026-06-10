package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByTenNguoiDung(String tenNguoiDung);

    List<NhanVien> findByTinhTrangLamViec(Byte tinhTrangLamViec);

    boolean existsByEmail(String email);

    boolean existsByTenNguoiDung(String tenNguoiDung);

    boolean existsByMaNhanVien(String maNhanVien);
}