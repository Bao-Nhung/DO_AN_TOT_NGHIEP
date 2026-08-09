package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    @EntityGraph(attributePaths = {"khachHang", "sanPham", "hoaDon"})
    Page<DanhGia> findBySanPhamIdAndTrangThai(Integer sanPhamId, Byte trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "sanPham", "hoaDon"})
    Page<DanhGia> findByTrangThai(Byte trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "sanPham", "hoaDon"})
    Page<DanhGia> findByTrangThaiAndSoSao(Byte trangThai, Byte soSao, Pageable pageable);

    boolean existsByKhachHangIdAndSanPhamIdAndHoaDonId(Integer khachHangId, Integer sanPhamId, Integer hoaDonId);

    @Query("SELECT COALESCE(AVG(d.soSao), 0) AS average, COUNT(d) AS total FROM DanhGia d WHERE d.sanPham.id = :productId AND d.trangThai = 1")
    ReviewSummary summarizeProduct(@Param("productId") Integer productId);

    @Query("""
            SELECT d.sanPham.id AS productId,
                   COALESCE(AVG(d.soSao), 0) AS average,
                   COUNT(d) AS total
            FROM DanhGia d
            WHERE d.trangThai = 1 AND d.sanPham.id IN :productIds
            GROUP BY d.sanPham.id
            """)
    List<ProductReviewSummary> summarizeProducts(@Param("productIds") List<Integer> productIds);

    @Query("SELECT COALESCE(AVG(d.soSao), 0) AS average, COUNT(d) AS total FROM DanhGia d WHERE d.trangThai = 1")
    ReviewSummary summarizeStore();

    @Query("SELECT d.soSao, COUNT(d) FROM DanhGia d WHERE d.sanPham.id = :productId AND d.trangThai = 1 GROUP BY d.soSao")
    List<Object[]> distributionForProduct(@Param("productId") Integer productId);

    @Query("SELECT d.soSao, COUNT(d) FROM DanhGia d WHERE d.trangThai = 1 GROUP BY d.soSao")
    List<Object[]> distributionForStore();

    @Query("""
            SELECT d.id AS id,
                   d.sanPham.id AS productId,
                   d.sanPham.tenSanPham AS productName,
                   d.khachHang.hoVaTen AS customerName,
                   d.soSao AS stars,
                   d.noiDung AS content,
                   d.ngayTao AS createdAt
            FROM DanhGia d
            WHERE d.trangThai = 1
            ORDER BY d.ngayTao DESC
            """)
    List<ReviewHighlight> findReviewHighlights(Pageable pageable);

    interface ReviewSummary {
        Double getAverage();
        Long getTotal();
    }

    interface ProductReviewSummary {
        Integer getProductId();
        Double getAverage();
        Long getTotal();
    }

    interface ReviewHighlight {
        Integer getId();
        Integer getProductId();
        String getProductName();
        String getCustomerName();
        Byte getStars();
        String getContent();
        LocalDateTime getCreatedAt();
    }
}
