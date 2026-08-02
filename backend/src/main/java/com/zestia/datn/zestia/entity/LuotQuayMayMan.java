package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Luot_quay_may_man", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Luot_quay_chien_dich_don", columnNames = {"id_chien_dich", "ma_hoa_don"}),
        @UniqueConstraint(name = "UQ_Luot_quay_ma_nhan", columnNames = "ma_nhan_thuong")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuotQuayMayMan {
    public static final String WAITING = "CHO_NHAN";
    public static final String DELIVERED = "DA_TRA";
    public static final String NO_PRIZE = "KHONG_TRUNG";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chien_dich", nullable = false)
    private VongQuayMayMan chienDich;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phan_thuong", nullable = false)
    private PhanThuongVongQuay phanThuong;

    @Column(name = "ma_hoa_don", nullable = false, length = 80)
    private String maHoaDon;

    @Column(name = "ten_khach_hang", nullable = false, length = 150)
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "email_khach_hang", length = 150)
    private String emailKhachHang;

    @Column(name = "gia_tri_don", nullable = false, precision = 15, scale = 2)
    private BigDecimal giaTriDon;

    @Column(name = "ten_ket_qua", nullable = false, length = 150)
    private String tenKetQua;

    @Column(name = "trung_thuong", nullable = false)
    private Boolean trungThuong;

    @Column(name = "ma_nhan_thuong", nullable = false, length = 30)
    private String maNhanThuong;

    @Column(name = "trang_thai_nhan", nullable = false, length = 20)
    private String trangThaiNhan;

    @Column(name = "ngay_quay", nullable = false)
    private LocalDateTime ngayQuay;

    @Column(name = "ngay_trao")
    private LocalDateTime ngayTrao;

    @Column(name = "nguoi_trao", length = 150)
    private String nguoiTrao;
}
