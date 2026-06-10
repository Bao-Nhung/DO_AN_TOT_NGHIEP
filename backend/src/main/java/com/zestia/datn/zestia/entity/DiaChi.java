package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Dia_chi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "tinh_thanh_pho", length = 100)
    private String tinhThanhPho;

    @Column(name = "quan_huyen", length = 100)
    private String quanHuyen;

    @Column(name = "xa_phuong", length = 100)
    private String xaPhuong;

    @Column(length = 255)
    private String duong;

    @Column(name = "mac_dinh")
    private Byte macDinh;
}
