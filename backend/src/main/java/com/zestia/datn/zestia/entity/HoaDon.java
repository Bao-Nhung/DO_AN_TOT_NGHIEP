package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_khuyen_mai")
    private KhuyenMai khuyenMai;

    @ManyToOne
    @JoinColumn(name = "id_giam_gia")
    private GiamGia giamGia;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    @Column(name = "giam_gia_khuyen_mai")
    private BigDecimal giamGiaKhuyenMai;

    @Column(name = "hinh_thuc_nhan_hang")
    private Byte hinhThucNhanHang;

    @Column(name = "dia_chi_giao_hang")
    private String diaChiGiaoHang;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;

    @Column(name = "phuong_thuc_thanh_toan_online")
    private String phuongThucThanhToanOnline;

    @Column(name = "da_thanh_toan")
    private Boolean daThanhToan;

    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @JsonIgnore
    @OneToMany(mappedBy = "hoaDon")
    private List<HoaDonChiTiet> chiTiets;
}
