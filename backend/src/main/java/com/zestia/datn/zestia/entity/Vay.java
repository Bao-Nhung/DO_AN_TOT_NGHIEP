package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Vay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_nha_cung_cap")
    private NhaCungCap nhaCungCap;

    @ManyToOne
    @JoinColumn(name = "id_loai_vay")
    private LoaiVay loaiVay;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @ManyToOne
    @JoinColumn(name = "id_tai_tro")
    private TaiTro taiTro;

    @Column(name = "ma_vay", unique = true)
    private String maVay;

    @Column(name = "ten_vay")
    private String tenVay;

    @Column(name = "link_youtube")
    private String linkYoutube;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "mo_ta", columnDefinition = "nvarchar(max)")
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "chieu_cao_nguoi_mau")
    private Integer chieuCaoNguoiMau;

    @Column(name = "can_nang_nguoi_mau")
    private Integer canNangNguoiMau;

    @Column(name = "size_nguoi_mau", length = 30)
    private String sizeNguoiMau;

    @Column(name = "mo_ta_phom", length = 500)
    private String moTaPhom;

    @JsonIgnore
    @OneToMany(mappedBy = "vay")
    private List<VayChiTiet> danhSachBienThe;

    @JsonIgnore
    @OneToMany(mappedBy = "vay")
    private List<Anh> danhSachAnh;
}
