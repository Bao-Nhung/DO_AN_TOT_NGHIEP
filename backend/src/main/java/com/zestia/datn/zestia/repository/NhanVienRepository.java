package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.NhanVien;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    @EntityGraph(attributePaths = "vaiTro")
    @Query(value = """
            SELECT nv FROM NhanVien nv
            LEFT JOIN nv.vaiTro vt
            WHERE (:keyword IS NULL
                   OR LOWER(nv.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.tenNguoiDung) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR nv.soDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:role IS NULL OR LOWER(vt.tenVaiTro) = LOWER(:role))
              AND (:status IS NULL OR nv.tinhTrangLamViec = :status)
            ORDER BY nv.ngayTao DESC, nv.id DESC
            """,
            countQuery = """
            SELECT COUNT(nv) FROM NhanVien nv
            LEFT JOIN nv.vaiTro vt
            WHERE (:keyword IS NULL
                   OR LOWER(nv.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.tenNguoiDung) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(nv.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR nv.soDienThoai LIKE CONCAT('%', :keyword, '%'))
              AND (:role IS NULL OR LOWER(vt.tenVaiTro) = LOWER(:role))
              AND (:status IS NULL OR nv.tinhTrangLamViec = :status)
            """)
    Page<NhanVien> findAdminPage(@Param("keyword") String keyword,
                                 @Param("role") String role,
                                 @Param("status") Byte status,
                                 Pageable pageable);

    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByEmailIgnoreCase(String email);

    Optional<NhanVien> findByTenNguoiDung(String tenNguoiDung);

    Optional<NhanVien> findByTenNguoiDungIgnoreCase(String tenNguoiDung);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT nv FROM NhanVien nv WHERE nv.id = :id")
    Optional<NhanVien> findByIdForUpdate(@Param("id") Integer id);

    List<NhanVien> findByTinhTrangLamViec(Byte tinhTrangLamViec);

    @Query("SELECT nv FROM NhanVien nv WHERE nv.matKhau IS NOT NULL AND nv.matKhau NOT LIKE '$2%'")
    List<NhanVien> findAccountsWithLegacyPassword();

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTenNguoiDung(String tenNguoiDung);

    boolean existsByTenNguoiDungIgnoreCase(String tenNguoiDung);

    boolean existsByMaNhanVien(String maNhanVien);

    long countByTinhTrangLamViec(Byte tinhTrangLamViec);

    long countByNgayTaoGreaterThanEqualAndNgayTaoLessThan(LocalDateTime startAt, LocalDateTime endAt);

    @Query("SELECT COUNT(nv) FROM NhanVien nv WHERE LOWER(nv.vaiTro.tenVaiTro) = 'admin'")
    long countAdmins();

    @Query("""
            SELECT COUNT(nv) FROM NhanVien nv
            WHERE LOWER(nv.vaiTro.tenVaiTro) = 'admin'
              AND nv.tinhTrangLamViec = 1
            """)
    long countActiveAdmins();
}
