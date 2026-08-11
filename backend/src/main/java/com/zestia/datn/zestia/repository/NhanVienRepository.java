package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByEmailIgnoreCase(String email);

    Optional<NhanVien> findByTenNguoiDung(String tenNguoiDung);

    Optional<NhanVien> findByTenNguoiDungIgnoreCase(String tenNguoiDung);

    List<NhanVien> findByTinhTrangLamViec(Byte tinhTrangLamViec);

    @Query("SELECT nv FROM NhanVien nv WHERE nv.matKhau IS NOT NULL AND nv.matKhau NOT LIKE '$2%'")
    List<NhanVien> findAccountsWithLegacyPassword();

    boolean existsByEmail(String email);

    boolean existsByTenNguoiDung(String tenNguoiDung);

    boolean existsByMaNhanVien(String maNhanVien);
}
