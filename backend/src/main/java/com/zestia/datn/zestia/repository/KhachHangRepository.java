package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT kh FROM KhachHang kh WHERE kh.id = :id")
    Optional<KhachHang> findByIdForUpdate(@Param("id") Integer id);

    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByEmailIgnoreCase(String email);

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    @Query("""
            SELECT kh FROM KhachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY kh.ngayTao DESC, kh.id DESC
            """)
    List<KhachHang> searchForPos(@Param("keyword") String keyword,
                                 org.springframework.data.domain.Pageable pageable);

    Optional<KhachHang> findByGoogleSubject(String googleSubject);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByMaKhachHang(String maKhachHang);

    boolean existsByGoogleSubject(String googleSubject);

    @Query("SELECT kh FROM KhachHang kh WHERE kh.matKhau IS NOT NULL AND kh.matKhau NOT LIKE '$2%'")
    List<KhachHang> findAccountsWithLegacyPassword();

    @Query("SELECT kh FROM KhachHang kh WHERE kh.email IS NOT NULL AND TRIM(kh.email) <> ''")
    Page<KhachHang> findAllWithEmail(Pageable pageable);

    @Query(value = """
            SELECT kh.id AS id,
                   kh.maKhachHang AS maKhachHang,
                   kh.hoVaTen AS hoVaTen,
                   kh.soDienThoai AS soDienThoai,
                   kh.email AS email,
                   kh.gioiTinh AS gioiTinh,
                   kh.diemTichLuy AS diemTichLuy,
                   kh.hangThanhVien AS hangThanhVien,
                   kh.ngayTao AS ngayTao,
                   COUNT(h.id) AS tongDon,
                   COALESCE(kh.tongChiTieu, 0) AS tongChiTieu
            FROM KhachHang kh
            LEFT JOIN HoaDon h ON h.khachHang.id = kh.id
            WHERE (:keyword IS NULL
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')))
            GROUP BY kh.id, kh.maKhachHang, kh.hoVaTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.diemTichLuy, kh.hangThanhVien, kh.tongChiTieu, kh.ngayTao
            ORDER BY kh.ngayTao DESC
            """,
            countQuery = """
            SELECT COUNT(kh) FROM KhachHang kh
            WHERE (:keyword IS NULL
                   OR LOWER(kh.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(kh.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
                   OR LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<KhachHangSummary> findCustomerSummaries(@org.springframework.data.repository.query.Param("keyword") String keyword,
                                                  Pageable pageable);

    interface KhachHangSummary {
        Integer getId();
        String getMaKhachHang();
        String getHoVaTen();
        String getSoDienThoai();
        String getEmail();
        Byte getGioiTinh();
        Integer getDiemTichLuy();
        String getHangThanhVien();
        Long getTongDon();
        BigDecimal getTongChiTieu();
        LocalDateTime getNgayTao();
    }
}
