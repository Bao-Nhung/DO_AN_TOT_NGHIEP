package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.BienDongTonKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BienDongTonKhoRepository extends JpaRepository<BienDongTonKho, Long> {

    @Query(value = "SELECT b FROM BienDongTonKho b " +
           "LEFT JOIN FETCH b.sanPhamChiTiet vc " +
           "LEFT JOIN FETCH vc.sanPham v " +
           "LEFT JOIN FETCH vc.mauSac m " +
           "LEFT JOIN FETCH vc.kichThuoc k " +
           "WHERE (:query IS NULL OR :query = '' OR LOWER(v.tenSanPham) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(v.maSanPham) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.loaiBienDong) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.maThamChieu) LIKE LOWER(CONCAT('%', :query, '%'))) ",
           countQuery = "SELECT COUNT(b) FROM BienDongTonKho b " +
           "LEFT JOIN b.sanPhamChiTiet vc LEFT JOIN vc.sanPham v " +
           "WHERE (:query IS NULL OR :query = '' OR LOWER(v.tenSanPham) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(v.maSanPham) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.loaiBienDong) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.maThamChieu) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<BienDongTonKho> findLogs(@Param("query") String query, Pageable pageable);
}
