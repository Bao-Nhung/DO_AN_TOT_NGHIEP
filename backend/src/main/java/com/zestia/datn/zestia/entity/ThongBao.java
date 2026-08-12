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

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "loai", length = 50)
    private String loai;

    @Column(name = "trang_thai")
    private Byte trangThai; // 1: Active/Sent, 0: Draft

    @Column(name = "gui_email")
    private Byte guiEmail; // 1: Gửi email, 0: Không gửi

    @Column(name = "da_gui")
    private Byte daGui; // 1: Đã gửi email, 0: Chưa gửi

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui; // Ngày giờ hẹn gửi

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
