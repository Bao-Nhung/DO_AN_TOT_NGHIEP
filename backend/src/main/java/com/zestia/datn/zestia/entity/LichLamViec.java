package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Lich_lam_viec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "ngay_lam_viec", nullable = false)
    private LocalDate ngayLamViec;

    @Column(name = "ca_lam_viec", length = 50)
    private String caLamViec;

    @Column(name = "gio_bat_dau")
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc")
    private LocalTime gioKetThuc;

    @Column(name = "vi_tri", length = 100)
    private String viTri;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
