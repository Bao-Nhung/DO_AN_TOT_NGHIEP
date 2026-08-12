package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {

    @EntityGraph(attributePaths = {
            "sanPhamChiTiet",
            "sanPhamChiTiet.sanPham",
            "sanPhamChiTiet.mauSac",
            "sanPhamChiTiet.kichThuoc"
    })
    List<GioHangChiTiet> findByGioHangId(Integer gioHangId);

    Optional<GioHangChiTiet> findByGioHangIdAndSanPhamChiTietId(
            Integer gioHangId,
            Integer sanPhamChiTietId
    );

    long countByGioHangId(Integer gioHangId);

    void deleteByGioHangId(Integer gioHangId);
}
