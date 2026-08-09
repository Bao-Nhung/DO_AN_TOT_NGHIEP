package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khach_hang", length = 50)
    private String maKhachHang;

    @Column(name = "ho_va_ten", nullable = false, length = 150)
    private String hoVaTen;

    @Column(name = "gioi_tinh")
    private Byte gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(length = 150)
    private String email;

    @Column(name = "google_subject", length = 100)
    private String googleSubject;

    @Column(name = "mat_khau", length = 255)
    private String matKhau;

    @Column(name = "diem_tich_luy")
    @Builder.Default
    private Integer diemTichLuy = 0;

    @Column(name = "tong_chi_tieu")
    @Builder.Default
    private java.math.BigDecimal tongChiTieu = java.math.BigDecimal.ZERO;

    @Column(name = "hang_thanh_vien", length = 50)
    @Builder.Default
    private String hangThanhVien = "Đồng";

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    // Quan hệ 1-1 với giỏ hàng
    @OneToOne(
            mappedBy = "khachHang",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private GioHang gioHang;
}
