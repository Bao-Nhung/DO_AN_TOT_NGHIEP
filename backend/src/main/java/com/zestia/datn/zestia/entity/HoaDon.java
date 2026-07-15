package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "Hoa_don",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_Hoa_don_ma", columnNames = "ma_hoa_don")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_giam_gia")
    private GiamGia giamGia;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "ma_hoa_don", nullable = false, length = 80)
    private String maHoaDon;

    @Column(name = "ma_yeu_cau", length = 100)
    private String maYeuCau;

    @Column(name = "ma_giao_dich_cong", length = 120)
    private String maGiaoDichCong;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    @Column(name = "giam_gia_voucher")
    private BigDecimal giamGiaVoucher;

    @Column(name = "hinh_thuc_nhan_hang")
    private Byte hinhThucNhanHang;

    @Column(name = "dia_chi_giao_hang")
    private String diaChiGiaoHang;

    @Column(name = "trang_thai")
    private Byte trangThai;

    // ==========================================
    // ===== TRACKING FIELDS (ĐỒNG ĐỘI THÊM) =====
    // ==========================================
    @Column(name = "trang_thai_tracking")
    private String trangThaiTracking; // pending, processing, shipped, delivered, cancelled

    @Column(name = "ngay_giao_hang_du_kien")
    private LocalDateTime ngayGiaoHangDuKien;

    @Column(name = "ngay_giao_hang_thuc_te")
    private LocalDateTime ngayGiaoHangThucTe;
    // ==========================================

    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;

    @Column(name = "phuong_thuc_thanh_toan_online")
    private String phuongThucThanhToanOnline;

    @Column(name = "da_thanh_toan")
    private Boolean daThanhToan;

    @Builder.Default
    @Column(name = "da_hoan_ton_kho", nullable = false)
    private Boolean daHoanTonKho = false;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "email_khach_hang")
    private String emailKhachHang;

    @JsonIgnore
    @Column(name = "huy_don_otp_hash", length = 100)
    private String huyDonOtpHash;

    @JsonIgnore
    @Column(name = "huy_don_otp_het_han")
    private LocalDateTime huyDonOtpHetHan;

    @JsonIgnore
    @Column(name = "huy_don_otp_so_lan_sai")
    private Integer huyDonOtpSoLanSai;

    @JsonIgnore
    @Column(name = "huy_don_otp_gui_luc")
    private LocalDateTime huyDonOtpGuiLuc;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thong_tin_hoan_tien", length = 500)
    private String thongTinHoanTien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @JsonIgnore
    @OneToMany(mappedBy = "hoaDon")
    private List<HoaDonChiTiet> chiTiets;

    // ==========================================
    // ===== RELATIONSHIP MỚI (ĐỒNG ĐỘI THÊM) ===
    // ==========================================
    @JsonIgnore
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LichSuTracking> lichSuTracking;
}
