package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Bien_dong_ton_kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienDongTonKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong_truoc", nullable = false)
    private Integer soLuongTruoc;

    @Column(name = "so_luong_thay_doi", nullable = false)
    private Integer soLuongThayDoi;

    @Column(name = "so_luong_sau", nullable = false)
    private Integer soLuongSau;

    @Column(name = "loai_bien_dong", nullable = false, length = 40)
    private String loaiBienDong;

    @Column(name = "ma_tham_chieu", length = 100)
    private String maThamChieu;

    @Column(name = "nguoi_thuc_hien", length = 150)
    private String nguoiThucHien;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
}
