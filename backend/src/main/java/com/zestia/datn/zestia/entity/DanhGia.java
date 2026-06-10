package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Danh_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vay", nullable = false)
    private Vay vay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @Column(name = "so_sao")
    private Byte soSao;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "anh_danh_gia")
    private String anhDanhGia;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
