package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Yeu_cau_doi_tra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YeuCauDoiTra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_hoa_don_chi_tiet", nullable = false)
    private HoaDonChiTiet hoaDonChiTiet;

    @ManyToOne
    @JoinColumn(name = "id_bien_the_doi")
    private SanPhamChiTiet bienTheDoi;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien_xu_ly")
    private NhanVien nhanVienXuLy;

    @Column(name = "loai_yeu_cau", nullable = false, length = 10)
    private String loaiYeuCau;

    @Column(name = "nguon", nullable = false, length = 10)
    private String nguon;

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "ly_do", nullable = false, length = 1000)
    private String lyDo;

    @Column(name = "tinh_trang_hang", length = 2000)
    private String tinhTrangHang;

    @Column(name = "thong_tin_hoan_tien", length = 500)
    private String thongTinHoanTien;

    @Column(name = "so_tien_hoan", precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "ma_giao_dich_hoan", length = 150)
    private String maGiaoDichHoan;

    @Column(name = "phan_hoi_cong", length = 2000)
    private String phanHoiCong;

    @Column(name = "ngay_yeu_cau_hoan")
    private LocalDateTime ngayYeuCauHoan;

    @Column(name = "ly_do_tu_choi", length = 1000)
    private String lyDoTuChoi;

    @Column(name = "ghi_chu_nhan_vien", length = 1000)
    private String ghiChuNhanVien;

    @Builder.Default
    @Column(name = "da_hoan_ton_kho", nullable = false)
    private Boolean daHoanTonKho = false;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @Column(name = "ngay_nhan_hang")
    private LocalDateTime ngayNhanHang;

    @Column(name = "ngay_hoan_tat")
    private LocalDateTime ngayHoanTat;
}
