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
    @EntityGraph(attributePaths = {"khachHang", "vay", "hoaDon"})
    Page<DanhGia> findByVayIdAndTrangThai(Integer vayId, Byte trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "vay", "hoaDon"})
    Page<DanhGia> findByTrangThai(Byte trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "vay", "hoaDon"})
    Page<DanhGia> findByTrangThaiAndSoSao(Byte trangThai, Byte soSao, Pageable pageable);

    boolean existsByKhachHangIdAndVayIdAndHoaDonId(Integer khachHangId, Integer vayId, Integer hoaDonId);

    @Query("SELECT COALESCE(AVG(d.soSao), 0) AS average, COUNT(d) AS total FROM DanhGia d WHERE d.vay.id = :productId AND d.trangThai = 1")
    ReviewSummary summarizeProduct(@Param("productId") Integer productId);

    @Query("""
            SELECT d.vay.id AS productId,
                   COALESCE(AVG(d.soSao), 0) AS average,
                   COUNT(d) AS total
            FROM DanhGia d
            WHERE d.trangThai = 1 AND d.vay.id IN :productIds
            GROUP BY d.vay.id
            """)
    List<ProductReviewSummary> summarizeProducts(@Param("productIds") List<Integer> productIds);

    @Query("SELECT COALESCE(AVG(d.soSao), 0) AS average, COUNT(d) AS total FROM DanhGia d WHERE d.trangThai = 1")
    ReviewSummary summarizeStore();

    @Query("SELECT d.soSao, COUNT(d) FROM DanhGia d WHERE d.vay.id = :productId AND d.trangThai = 1 GROUP BY d.soSao")
    List<Object[]> distributionForProduct(@Param("productId") Integer productId);

    @Query("SELECT d.soSao, COUNT(d) FROM DanhGia d WHERE d.trangThai = 1 GROUP BY d.soSao")
    List<Object[]> distributionForStore();

    @Query("""
            SELECT d.id AS id,
                   d.vay.id AS productId,
                   d.vay.tenVay AS productName,
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
