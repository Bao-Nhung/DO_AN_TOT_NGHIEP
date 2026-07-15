package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Chinh_sach_cua_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChinhSachCuaHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chinh_sach", nullable = false, unique = true, length = 50)
    private String maChinhSach;

    @Column(name = "tieu_de", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "tom_tat", length = 500)
    private String tomTat;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "gia_tri_so")
    private Integer giaTriSo;

    @Column(name = "don_vi", length = 30)
    private String donVi;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
}
