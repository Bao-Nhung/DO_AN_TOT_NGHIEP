package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Gio_hang_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gio_hang", nullable = false)
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vay_chi_tiet", nullable = false)
    private VayChiTiet vayChiTiet;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}