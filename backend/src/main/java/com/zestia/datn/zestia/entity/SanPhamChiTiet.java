package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    @Column(name = "ma_san_pham_chi_tiet")
    private String maSanPhamChiTiet;

    @Column(name = "gia_ban")
    private BigDecimal giaBan;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "anh_url")
    private String anhUrl;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

}
