package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_nha_cung_cap")
    private NhaCungCap nhaCungCap;

    @ManyToOne
    @JoinColumn(name = "id_loai_san_pham")
    private LoaiSanPham loaiSanPham;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @ManyToOne
    @JoinColumn(name = "id_tai_tro")
    private TaiTro taiTro;

    @Column(name = "ma_san_pham", unique = true)
    private String maSanPham;

    @Column(name = "ten_san_pham")
    private String tenSanPham;

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
    @OneToMany(mappedBy = "sanPham")
    private List<SanPhamChiTiet> danhSachBienThe;

    @JsonIgnore
    @OneToMany(mappedBy = "sanPham")
    private List<Anh> danhSachAnh;

    public String getTenVay() { return tenSanPham; }
    public void setTenVay(String val) { this.tenSanPham = val; }
    public String getMaVay() { return maSanPham; }
    public void setMaVay(String val) { this.maSanPham = val; }
    public LoaiSanPham getLoaiVay() { return loaiSanPham; }
    public void setLoaiVay(LoaiSanPham val) { this.loaiSanPham = val; }
}
