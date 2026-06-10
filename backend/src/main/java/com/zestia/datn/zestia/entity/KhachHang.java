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

    @Column(name = "ma_khach_hang", unique = true, length = 50)
    private String maKhachHang;

    @Column(name = "ho_va_ten", nullable = false, length = 150)
    private String hoVaTen;

    @Column(name = "gioi_tinh")
    private Byte gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "so_dien_thoai", unique = true, length = 20)
    private String soDienThoai;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "mat_khau", length = 255)
    private String matKhau;

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