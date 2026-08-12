package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.YeuCauDoiTra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface YeuCauDoiTraRepository extends JpaRepository<YeuCauDoiTra, Integer> {

    @EntityGraph(attributePaths = {
            "hoaDon", "hoaDonChiTiet", "hoaDonChiTiet.sanPhamChiTiet",
            "hoaDonChiTiet.sanPhamChiTiet.sanPham", "hoaDonChiTiet.sanPhamChiTiet.mauSac",
            "hoaDonChiTiet.sanPhamChiTiet.kichThuoc", "bienTheDoi", "bienTheDoi.sanPham",
            "bienTheDoi.mauSac", "bienTheDoi.kichThuoc", "khachHang", "nhanVienXuLy"
    })
    List<YeuCauDoiTra> findByKhachHangIdOrderByNgayTaoDesc(Integer customerId);

    @EntityGraph(attributePaths = {
            "hoaDon", "hoaDonChiTiet", "hoaDonChiTiet.sanPhamChiTiet",
            "hoaDonChiTiet.sanPhamChiTiet.sanPham", "hoaDonChiTiet.sanPhamChiTiet.mauSac",
            "hoaDonChiTiet.sanPhamChiTiet.kichThuoc", "bienTheDoi", "bienTheDoi.sanPham",
            "bienTheDoi.mauSac", "bienTheDoi.kichThuoc", "khachHang", "nhanVienXuLy"
    })
    @Query(value = """
            SELECT r FROM YeuCauDoiTra r
            WHERE r.khachHang.id = :customerId
              AND (:keyword IS NULL OR LOWER(r.hoaDon.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(r) FROM YeuCauDoiTra r
            WHERE r.khachHang.id = :customerId
              AND (:keyword IS NULL OR LOWER(r.hoaDon.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<YeuCauDoiTra> findCustomerPage(@Param("customerId") Integer customerId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @EntityGraph(attributePaths = {
            "hoaDon", "hoaDonChiTiet", "hoaDonChiTiet.sanPhamChiTiet",
            "hoaDonChiTiet.sanPhamChiTiet.sanPham", "hoaDonChiTiet.sanPhamChiTiet.mauSac",
            "hoaDonChiTiet.sanPhamChiTiet.kichThuoc", "bienTheDoi", "bienTheDoi.sanPham",
            "bienTheDoi.mauSac", "bienTheDoi.kichThuoc", "khachHang", "nhanVienXuLy"
    })
    @Query("""
            SELECT r FROM YeuCauDoiTra r
            WHERE (:type IS NULL OR r.loaiYeuCau = :type)
              AND (:status IS NULL OR r.trangThai = :status)
            """)
    Page<YeuCauDoiTra> findForAdmin(@Param("type") String type,
                                    @Param("status") String status,
                                    Pageable pageable);

    List<YeuCauDoiTra> findByHoaDonIdInOrderByNgayTaoDesc(List<Integer> orderIds);

    Optional<YeuCauDoiTra> findFirstByHoaDonIdOrderByNgayTaoDesc(Integer orderId);

    long countByTrangThaiIn(Collection<String> statuses);

    Optional<YeuCauDoiTra> findFirstByHoaDonChiTietIdOrderByNgayTaoDesc(Integer orderDetailId);

    @Query("""
            SELECT COALESCE(SUM(r.soLuong), 0)
            FROM YeuCauDoiTra r
            WHERE r.hoaDonChiTiet.id = :orderDetailId
              AND r.trangThai IN :statuses
            """)
    Long sumQuantityByOrderDetailAndStatuses(@Param("orderDetailId") Integer orderDetailId,
                                             @Param("statuses") Collection<String> statuses);

    @Query("""
            SELECT r.hoaDonChiTiet.id, COALESCE(SUM(r.soLuong), 0)
            FROM YeuCauDoiTra r
            WHERE r.hoaDonChiTiet.id IN :orderDetailIds
              AND r.trangThai IN :statuses
            GROUP BY r.hoaDonChiTiet.id
            """)
    List<Object[]> sumQuantitiesByOrderDetailsAndStatuses(
            @Param("orderDetailIds") Collection<Integer> orderDetailIds,
            @Param("statuses") Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM YeuCauDoiTra r WHERE r.id = :id")
    Optional<YeuCauDoiTra> findByIdForUpdate(@Param("id") Integer id);
}
