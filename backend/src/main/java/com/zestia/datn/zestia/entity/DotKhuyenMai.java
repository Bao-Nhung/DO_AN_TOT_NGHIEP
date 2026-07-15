package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Dot_khuyen_mai", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Dot_khuyen_mai_ma", columnNames = "ma_dot")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_dot", nullable = false, length = 50)
    private String maDot;

    @Column(name = "ten_dot", nullable = false, length = 150)
    private String tenDot;

    @Column(name = "loai_giam", nullable = false, length = 20)
    private String loaiGiam;

    @Column(name = "gia_tri_giam", nullable = false, precision = 15, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Builder.Default
    @Column(name = "trang_thai", nullable = false)
    private Byte trangThai = 1;

    @Builder.Default
    @Column(name = "do_uu_tien", nullable = false)
    private Integer doUuTien = 0;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "dotKhuyenMai", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PhamViKhuyenMai> phamVis = new ArrayList<>();
}
