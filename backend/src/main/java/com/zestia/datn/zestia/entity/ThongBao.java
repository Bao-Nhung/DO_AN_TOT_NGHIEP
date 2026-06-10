package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    private String loai;

    @Column(name = "da_doc")
    private Byte daDoc;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
