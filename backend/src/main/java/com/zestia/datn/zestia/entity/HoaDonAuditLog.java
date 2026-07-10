package com.zestia.datn.zestia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Hoa_don_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @Column(name = "hanh_dong", length = 80)
    private String hanhDong;

    @Column(name = "trang_thai_cu")
    private Byte trangThaiCu;

    @Column(name = "trang_thai_moi")
    private Byte trangThaiMoi;

    @Column(name = "nguoi_thuc_hien")
    private String nguoiThucHien;

    @Column(name = "vai_tro", length = 40)
    private String vaiTro;

    @Column(name = "ghi_chu", columnDefinition = "nvarchar(max)")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
