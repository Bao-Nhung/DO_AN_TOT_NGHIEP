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

    List<HoaDonChiTiet> findByVayChiTietId(Integer vayChiTietId);

    void deleteByHoaDonId(Integer hoaDonId);

    long countByHoaDonId(Integer hoaDonId);

    @Query("SELECT ct.hoaDon.id, COUNT(ct) FROM HoaDonChiTiet ct WHERE ct.hoaDon.id IN :orderIds GROUP BY ct.hoaDon.id")
    List<Object[]> countItemsByOrderIds(@Param("orderIds") List<Integer> orderIds);

    @Query("""
            SELECT DISTINCT h.id FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.khachHang.id = :customerId
              AND ct.vayChiTiet.vay.id = :productId
              AND h.trangThai = 4
            ORDER BY h.id DESC
            """)
    List<Integer> findDeliveredOrderIdsForCustomerAndProduct(@Param("customerId") Integer customerId,
                                                              @Param("productId") Integer productId);

    @Query("""
            SELECT ct.vayChiTiet.vay.id, SUM(ct.soLuong)
            FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.trangThai = 4
              AND ct.vayChiTiet.vay.trangThai = 1
            GROUP BY ct.vayChiTiet.vay.id
            ORDER BY SUM(ct.soLuong) DESC
            """)
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("""
            SELECT ct.vayChiTiet.vay.id, ct.vayChiTiet.vay.maVay,
                   ct.vayChiTiet.vay.tenVay, SUM(ct.soLuong),
                   SUM(ct.donGia * ct.soLuong)
            FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE h.trangThai = 4
               OR (h.daThanhToan = true AND h.trangThai NOT IN (5, 6, 7, 9))
            GROUP BY ct.vayChiTiet.vay.id, ct.vayChiTiet.vay.maVay, ct.vayChiTiet.vay.tenVay
            ORDER BY SUM(ct.soLuong) DESC
            """)
    List<Object[]> findTopSellingProductStats(Pageable pageable);
}
