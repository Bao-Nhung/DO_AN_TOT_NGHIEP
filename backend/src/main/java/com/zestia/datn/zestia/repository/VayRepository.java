package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.Vay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VayRepository extends JpaRepository<Vay, Integer> {

    Optional<Vay> findByMaVay(String maVay);

    List<Vay> findByTenVayContainingIgnoreCase(String tenVay);

    List<Vay> findByTrangThai(Byte trangThai);

    List<Vay> findByLoaiVayId(Integer loaiVayId);

    boolean existsByMaVay(String maVay);

    long countByTrangThai(Byte trangThai);

    @EntityGraph(attributePaths = {"loaiVay", "chatLieu", "nhaCungCap"})
    @Query("SELECT v FROM Vay v WHERE v.trangThai = 1 ORDER BY v.ngayTao DESC, v.id DESC")
    List<Vay> findActiveForAi(Pageable pageable);

    @EntityGraph(attributePaths = {"loaiVay", "chatLieu", "nhaCungCap"})
    @Query(value = """
            SELECT v FROM Vay v
            LEFT JOIN v.loaiVay lv
            WHERE (:keyword IS NULL
                   OR LOWER(v.tenVay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.maVay) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR v.trangThai = :status)
              AND (:category IS NULL
                   OR LOWER(lv.tenLoaiVay) LIKE LOWER(CONCAT('%', :category, '%')))
            """,
            countQuery = """
            SELECT COUNT(v) FROM Vay v
            LEFT JOIN v.loaiVay lv
            WHERE (:keyword IS NULL
                   OR LOWER(v.tenVay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.maVay) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR v.trangThai = :status)
              AND (:category IS NULL
                   OR LOWER(lv.tenLoaiVay) LIKE LOWER(CONCAT('%', :category, '%')))
            """)
    Page<Vay> findAdminPage(@Param("keyword") String keyword,
                            @Param("status") Byte status,
                            @Param("category") String category,
                            Pageable pageable);
}
