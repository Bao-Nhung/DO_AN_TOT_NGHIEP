package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Thong_bao_da_doc",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_ThongBaoDaDoc_KhachHang_ThongBao",
                columnNames = {"id_khach_hang", "id_thong_bao"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBaoDaDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_thong_bao", nullable = false)
    private ThongBao thongBao;

    @Column(name = "ngay_doc", nullable = false)
    private LocalDateTime ngayDoc;
}
