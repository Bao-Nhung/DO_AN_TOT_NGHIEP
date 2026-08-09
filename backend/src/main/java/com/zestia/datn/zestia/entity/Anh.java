package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Anh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "anh_url")
    private String anhUrl;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public SanPham getVay() { return sanPham; }
    public void setVay(SanPham sp) { this.sanPham = sp; }
}
