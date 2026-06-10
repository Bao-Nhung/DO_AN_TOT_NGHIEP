package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Gio_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false, unique = true)
    private KhachHang khachHang;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(
            mappedBy = "gioHang",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<GioHangChiTiet> chiTiets;
}