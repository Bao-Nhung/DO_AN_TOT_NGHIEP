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

    @Column(name = "ngay_lam", nullable = false)
    private LocalDate ngayLam;

    @Column(name = "ca_lam", length = 50)
    private String caLam;

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ly_do_bao_ban", length = 500)
    private String lyDoBaoBan;

    @Column(name = "phan_hoi_bao_ban", length = 500)
    private String phanHoiBaoBan;

    @Column(name = "thoi_gian_xac_nhan")
    private LocalDateTime thoiGianXacNhan;

    @Column(name = "thoi_gian_bao_ban")
    private LocalDateTime thoiGianBaoBan;

    @Column(name = "thoi_gian_duyet")
    private LocalDateTime thoiGianDuyet;

    @Column(name = "nguoi_duyet", length = 150)
    private String nguoiDuyet;

    @Column(name = "gio_check_in")
    private LocalDateTime gioCheckIn;

    @Column(name = "gio_check_out")
    private LocalDateTime gioCheckOut;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
