package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_giam_gia")
    private String maGiamGia;

    @Column(name = "ten_giam_gia")
    private String tenGiamGia;

    @Column(name = "gia_tri_don_toi_thieu")
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "gio_tri_giam")
    private BigDecimal gioTriGiam;

    @Column(name = "phan_tram_giam")
    private BigDecimal phanTramGiam;

    @Column(name = "giam_toi_da")
    private BigDecimal giamToiDa;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
