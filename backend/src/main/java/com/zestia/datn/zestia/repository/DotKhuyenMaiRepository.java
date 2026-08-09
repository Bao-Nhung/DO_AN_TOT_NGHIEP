package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.DotKhuyenMai;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DotKhuyenMaiRepository extends JpaRepository<DotKhuyenMai, Integer> {
    boolean existsByMaDotIgnoreCase(String maDot);
    boolean existsByMaDotIgnoreCaseAndIdNot(String maDot, Integer id);
    Optional<DotKhuyenMai> findByMaDotIgnoreCase(String maDot);

    @EntityGraph(attributePaths = {
            "phamVis", "phamVis.sanPham", "phamVis.loaiSanPham", "phamVis.mauSac", "phamVis.kichThuoc"
    })
    @Query("""
            SELECT DISTINCT campaign FROM DotKhuyenMai campaign
            WHERE campaign.trangThai = 1
              AND campaign.ngayBatDau <= :now
              AND campaign.ngayKetThuc >= :now
            ORDER BY campaign.doUuTien DESC, campaign.id DESC
            """)
    List<DotKhuyenMai> findActiveAt(@Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {
            "phamVis", "phamVis.sanPham", "phamVis.loaiSanPham", "phamVis.mauSac", "phamVis.kichThuoc"
    })
    @Query("SELECT DISTINCT campaign FROM DotKhuyenMai campaign ORDER BY campaign.ngayBatDau DESC, campaign.id DESC")
    List<DotKhuyenMai> findAllDetailed();

    @EntityGraph(attributePaths = {
            "phamVis", "phamVis.sanPham", "phamVis.loaiSanPham", "phamVis.mauSac", "phamVis.kichThuoc"
    })
    @Query("SELECT DISTINCT campaign FROM DotKhuyenMai campaign WHERE campaign.id = :id")
    Optional<DotKhuyenMai> findDetailedById(@Param("id") Integer id);
}
