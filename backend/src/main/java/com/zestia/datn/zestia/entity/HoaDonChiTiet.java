package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Hoa_don_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet")
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_gia")
    private BigDecimal donGia;

    @Column(name = "phan_tram_giam")
    private BigDecimal phanTramGiam;

    @Column(name = "thanh_tien")
    private BigDecimal thanhTien;

    @Column(name = "ma_san_pham_snapshot", length = 50)
    private String maSanPhamSnapshot;

    @Column(name = "ten_san_pham_snapshot", length = 200)
    private String tenSanPhamSnapshot;

    @Column(name = "mau_sac_snapshot", length = 100)
    private String mauSacSnapshot;

    @Column(name = "kich_thuoc_snapshot", length = 50)
    private String kichThuocSnapshot;

    @Column(name = "anh_url_snapshot", length = 500)
    private String anhUrlSnapshot;

}
