package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Mau_Sac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MauSac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_mau_sac", nullable = false, length = 100)
    private String tenMauSac;

    @Column(name = "ma_hex", length = 7)
    private String maHex;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
