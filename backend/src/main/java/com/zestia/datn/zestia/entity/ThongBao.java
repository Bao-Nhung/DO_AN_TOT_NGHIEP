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

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "loai", length = 50)
    private String loai;

    @Column(name = "trang_thai")
    private Byte trangThai; // 1: Active/Sent, 0: Draft

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}