package com.zestia.datn.zestia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Lich_su_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @Column(name = "trang_thai")
    private String trangThai; // pending, processing, shipped, delivered, cancelled

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
}