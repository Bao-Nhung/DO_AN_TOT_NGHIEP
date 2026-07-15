package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "Huong_dan_kich_thuoc",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_Huong_dan_size_Vay_KichThuoc",
                columnNames = {"id_vay", "id_kich_thuoc"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HuongDanKichThuoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vay", nullable = false)
    private Vay vay;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_kich_thuoc", nullable = false)
    private KichThuoc kichThuoc;

    @Column(name = "chieu_cao_tu")
    private Integer chieuCaoTu;

    @Column(name = "chieu_cao_den")
    private Integer chieuCaoDen;

    @Column(name = "can_nang_tu")
    private Integer canNangTu;

    @Column(name = "can_nang_den")
    private Integer canNangDen;

    @Column(name = "vong_nguc_tu")
    private Integer vongNgucTu;

    @Column(name = "vong_nguc_den")
    private Integer vongNgucDen;

    @Column(name = "vong_eo_tu")
    private Integer vongEoTu;

    @Column(name = "vong_eo_den")
    private Integer vongEoDen;

    @Column(name = "vong_mong_tu")
    private Integer vongMongTu;

    @Column(name = "vong_mong_den")
    private Integer vongMongDen;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;
}
