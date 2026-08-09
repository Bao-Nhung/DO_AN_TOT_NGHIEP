package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Loai_san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_loai_san_pham", nullable = false, length = 150)
    private String tenLoaiSanPham;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public String getTenLoaiVay() { return tenLoaiSanPham; }
    public void setTenLoaiVay(String val) { this.tenLoaiSanPham = val; }
}
