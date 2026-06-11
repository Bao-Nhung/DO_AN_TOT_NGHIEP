package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vai_tro")
    private VaiTro vaiTro;

    @Column(name = "ma_nhan_vien", unique = true, length = 50)
    private String maNhanVien;

    @Column(name = "ho_va_ten", nullable = false, length = 150)
    private String hoVaTen;

    @Column(name = "gioi_tinh")
    private Byte gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "ten_nguoi_dung", unique = true, length = 100)
    private String tenNguoiDung;

    @Column(name = "mat_khau", length = 255)
    private String matKhau;

    @Column(name = "tinh_trang_lam_viec")
    private Byte tinhTrangLamViec;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
