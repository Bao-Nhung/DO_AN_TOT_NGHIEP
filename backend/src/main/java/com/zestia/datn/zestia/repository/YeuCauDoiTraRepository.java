package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.YeuCauDoiTra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface YeuCauDoiTraRepository extends JpaRepository<YeuCauDoiTra, Integer> {

    @EntityGraph(attributePaths = {
            "hoaDon", "hoaDonChiTiet", "hoaDonChiTiet.vayChiTiet",
            "hoaDonChiTiet.vayChiTiet.vay", "hoaDonChiTiet.vayChiTiet.mauSac",
            "hoaDonChiTiet.vayChiTiet.kichThuoc", "bienTheDoi", "bienTheDoi.vay",
            "bienTheDoi.mauSac", "bienTheDoi.kichThuoc", "khachHang", "nhanVienXuLy"
    })
    List<YeuCauDoiTra> findByKhachHangIdOrderByNgayTaoDesc(Integer customerId);

    @EntityGraph(attributePaths = {
            "hoaDon", "hoaDonChiTiet", "hoaDonChiTiet.vayChiTiet",
            "hoaDonChiTiet.vayChiTiet.vay", "hoaDonChiTiet.vayChiTiet.mauSac",
            "hoaDonChiTiet.vayChiTiet.kichThuoc", "bienTheDoi", "bienTheDoi.vay",
            "bienTheDoi.mauSac", "bienTheDoi.kichThuoc", "khachHang", "nhanVienXuLy"
    })
    List<YeuCauDoiTra> findAllByOrderByNgayTaoDesc();

    List<YeuCauDoiTra> findByHoaDonIdInOrderByNgayTaoDesc(List<Integer> orderIds);

    Optional<YeuCauDoiTra> findFirstByHoaDonIdOrderByNgayTaoDesc(Integer orderId);

    boolean existsByHoaDonChiTietIdAndTrangThaiIn(Integer orderDetailId, Collection<String> statuses);

    boolean existsByHoaDonChiTietId(Integer orderDetailId);

    Optional<YeuCauDoiTra> findByHoaDonChiTietId(Integer orderDetailId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM YeuCauDoiTra r WHERE r.id = :id")
    Optional<YeuCauDoiTra> findByIdForUpdate(@Param("id") Integer id);
}
