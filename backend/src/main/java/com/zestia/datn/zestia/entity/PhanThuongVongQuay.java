package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Phan_thuong_vong_quay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanThuongVongQuay {
    public static final String PHYSICAL = "VAT_PHAM";
    public static final String NO_PRIZE = "KHONG_TRUNG";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chien_dich", nullable = false)
    private VongQuayMayMan chienDich;

    @Column(name = "ten_phan_thuong", nullable = false, length = 150)
    private String tenPhanThuong;

    @Column(name = "loai_phan_thuong", nullable = false, length = 20)
    private String loaiPhanThuong;

    @Column(name = "so_luong_ban_dau")
    private Integer soLuongBanDau;

    @Column(name = "so_luong_con")
    private Integer soLuongCon;

    @Column(name = "trong_so", nullable = false)
    private Integer trongSo;

    @Column(name = "mau_hien_thi", nullable = false, length = 7)
    private String mauHienThi;

    @Column(name = "bieu_tuong", nullable = false, length = 50)
    private String bieuTuong;

    @Column(name = "anh_bieu_tuong", length = 500)
    private String anhBieuTuong;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu;

    @Builder.Default
    @Column(name = "trang_thai", nullable = false)
    private Byte trangThai = 1;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
}
