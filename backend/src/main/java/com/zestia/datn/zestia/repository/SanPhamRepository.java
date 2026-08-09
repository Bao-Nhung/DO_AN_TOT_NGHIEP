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
            """)
    Page<SanPham> findAdminPage(@Param("keyword") String keyword,
                                @Param("status") Byte status,
                                @Param("category") String category,
                                Pageable pageable);
}
