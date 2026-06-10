package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Anh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_vay")
    private Vay vay;

    @Column(name = "anh_url")
    private String anhUrl;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
