package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Khuyen_mai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai")
    private String maKhuyenMai;

    @Column(name = "ten_khuyen_mai")
    private String tenKhuyenMai;

    @Column(name = "phan_tram_giam")
    private BigDecimal phanTramGiam;

    @Column(name = "so_tien_giam")
    private BigDecimal soTienGiam;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    private String moTa;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
