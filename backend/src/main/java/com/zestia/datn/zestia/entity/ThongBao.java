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

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "loai_thong_bao")
    private String loaiThongBao; // order_created, order_cancelled, order_shipped, order_delivered, add_to_cart

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "da_doc")
    private Byte daDoc; // 0 = chưa đọc, 1 = đã đọc

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    public void prePersist() {
        if (this.daDoc == null) {
            this.daDoc = 0;
        }
        if (this.ngayTao == null) {
            this.ngayTao = LocalDateTime.now();
        }
    }
}