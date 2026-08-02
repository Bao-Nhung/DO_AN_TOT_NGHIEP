package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Vong_quay_may_man", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Vong_quay_ma", columnNames = "ma_chien_dich")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VongQuayMayMan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chien_dich", nullable = false, length = 50)
    private String maChienDich;

    @Column(name = "ten_chien_dich", nullable = false, length = 150)
    private String tenChienDich;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "gia_tri_don_toi_thieu", nullable = false, precision = 15, scale = 2)
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Builder.Default
    @Column(name = "trang_thai", nullable = false)
    private Byte trangThai = 1;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "chienDich", cascade = CascadeType.ALL)
    private List<PhanThuongVongQuay> phanThuongs = new ArrayList<>();
}
