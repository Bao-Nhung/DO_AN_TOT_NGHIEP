package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    Optional<SanPham> findByMaSanPham(String maSanPham);

    List<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham);

    List<SanPham> findByTrangThai(Byte trangThai);

    List<SanPham> findByLoaiSanPhamId(Integer loaiSanPhamId);

    boolean existsByLoaiSanPhamIdAndTrangThai(Integer loaiSanPhamId, Byte trangThai);

    boolean existsByChatLieuIdAndTrangThai(Integer chatLieuId, Byte trangThai);

    boolean existsByNhaCungCapIdAndTrangThai(Integer nhaCungCapId, Byte trangThai);

    boolean existsByMaSanPham(String maSanPham);

    long countByTrangThai(Byte trangThai);

    @EntityGraph(attributePaths = {"loaiSanPham", "chatLieu", "nhaCungCap"})
    @Query("SELECT v FROM SanPham v WHERE v.trangThai = 1 ORDER BY v.ngayTao DESC, v.id DESC")
    List<SanPham> findActiveForAi(Pageable pageable);

    @EntityGraph(attributePaths = {"loaiSanPham", "chatLieu", "nhaCungCap"})
    @Query(value = """
            SELECT v FROM SanPham v
            LEFT JOIN v.loaiSanPham lv
            WHERE (:keyword IS NULL
                   OR LOWER(v.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR v.trangThai = :status)
              AND (:category IS NULL
                   OR LOWER(lv.tenLoaiSanPham) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (
                   :stock IS NULL
                   OR (:stock = 'HEALTHY' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) > 5)
                   OR (:stock = 'LOW' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) BETWEEN 1 AND 5)
                   OR (:stock = 'OUT' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) <= 0)
              )
            """,
            countQuery = """
            SELECT COUNT(v) FROM SanPham v
            LEFT JOIN v.loaiSanPham lv
            WHERE (:keyword IS NULL
                   OR LOWER(v.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR v.trangThai = :status)
              AND (:category IS NULL
                   OR LOWER(lv.tenLoaiSanPham) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (
                   :stock IS NULL
                   OR (:stock = 'HEALTHY' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) > 5)
                   OR (:stock = 'LOW' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) BETWEEN 1 AND 5)
                   OR (:stock = 'OUT' AND (
                       SELECT COALESCE(SUM(COALESCE(variant.soLuong, 0)), 0)
                       FROM SanPhamChiTiet variant
                       WHERE variant.sanPham = v AND variant.trangThai = 1
                   ) <= 0)
              )
            """)
    Page<SanPham> findAdminPage(@Param("keyword") String keyword,
                                @Param("status") Byte status,
                                @Param("category") String category,
                                @Param("stock") String stock,
                                Pageable pageable);
}
