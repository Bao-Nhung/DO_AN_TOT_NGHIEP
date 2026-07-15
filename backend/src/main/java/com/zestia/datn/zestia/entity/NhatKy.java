package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Nhat_ky")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "hanh_dong")
    private String hanhDong;

    @Column(name = "bang_tac_dong")
    private String bangTacDong;

    @Column(name = "id_ban_ghi")
    private Integer idBanGhi;

    @Column(name = "chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String chiTiet;

    @Column(name = "dia_chi_ip")
    private String diaChiIp;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
