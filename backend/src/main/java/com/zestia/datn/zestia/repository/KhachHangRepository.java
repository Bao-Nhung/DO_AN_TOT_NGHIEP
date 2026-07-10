package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByMaKhachHang(String maKhachHang);

    @Query("""
            SELECT kh.id AS id,
                   kh.maKhachHang AS maKhachHang,
                   kh.hoVaTen AS hoVaTen,
                   kh.soDienThoai AS soDienThoai,
                   kh.email AS email,
                   kh.gioiTinh AS gioiTinh,
                   kh.ngayTao AS ngayTao,
                   COUNT(h.id) AS tongDon,
                   COALESCE(SUM(h.tongTien), 0) AS tongChiTieu
            FROM KhachHang kh
            LEFT JOIN HoaDon h ON h.khachHang.id = kh.id
            GROUP BY kh.id, kh.maKhachHang, kh.hoVaTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngayTao
            ORDER BY kh.ngayTao DESC
            """)
    List<KhachHangSummary> findCustomerSummaries();

    interface KhachHangSummary {
        Integer getId();
        String getMaKhachHang();
        String getHoVaTen();
        String getSoDienThoai();
        String getEmail();
        Byte getGioiTinh();
        Long getTongDon();
        BigDecimal getTongChiTieu();
        LocalDateTime getNgayTao();
    }
}
