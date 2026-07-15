package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Lich_su_xem",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_Lich_su_xem_Khach_Vay",
                columnNames = {"id_khach_hang", "id_vay"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuXem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vay", nullable = false)
    private Vay vay;

    @Column(name = "ngay_xem")
    private LocalDateTime ngayXem;
}
