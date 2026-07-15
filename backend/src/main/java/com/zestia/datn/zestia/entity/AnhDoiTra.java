package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Anh_doi_tra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnhDoiTra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_yeu_cau", nullable = false)
    private YeuCauDoiTra yeuCau;

    @Column(name = "anh_url", nullable = false, length = 500)
    private String anhUrl;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
}
