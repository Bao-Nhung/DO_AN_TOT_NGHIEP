package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    List<HoaDonChiTiet> findByHoaDonIdIn(List<Integer> hoaDonIds);

    List<HoaDonChiTiet> findBySanPhamChiTietId(Integer sanPhamChiTietId);

    void deleteByHoaDonId(Integer hoaDonId);

    long countByHoaDonId(Integer hoaDonId);

    @Query("SELECT ct.hoaDon.id, COUNT(ct) FROM HoaDonChiTiet ct WHERE ct.hoaDon.id IN :orderIds GROUP BY ct.hoaDon.id")
    List<Object[]> countItemsByOrderIds(@Param("orderIds") List<Integer> orderIds);

    @Query("""
            SELECT DISTINCT h.id FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.khachHang.id = :customerId
              AND ct.sanPhamChiTiet.sanPham.id = :productId
              AND h.trangThai = 4
            ORDER BY h.id DESC
            """)
    List<Integer> findDeliveredOrderIdsForCustomerAndProduct(@Param("customerId") Integer customerId,
                                                              @Param("productId") Integer productId);

    @Query("""
            SELECT ct.sanPhamChiTiet.sanPham.id, SUM(ct.soLuong)
            FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.trangThai = 4
              AND ct.sanPhamChiTiet.sanPham.trangThai = 1
            GROUP BY ct.sanPhamChiTiet.sanPham.id
            ORDER BY SUM(ct.soLuong) DESC
            """)
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("""
            SELECT ct.sanPhamChiTiet.sanPham.id, ct.sanPhamChiTiet.sanPham.maSanPham,
                   ct.sanPhamChiTiet.sanPham.tenSanPham, SUM(ct.soLuong),
                   SUM(ct.donGia * ct.soLuong), MIN(ct.sanPhamChiTiet.anhUrl)
            FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.trangThai = 4
            GROUP BY ct.sanPhamChiTiet.sanPham.id, ct.sanPhamChiTiet.sanPham.maSanPham, ct.sanPhamChiTiet.sanPham.tenSanPham
            ORDER BY SUM(ct.soLuong) DESC
            """)
    List<Object[]> findTopSellingProductStats(Pageable pageable);
}
