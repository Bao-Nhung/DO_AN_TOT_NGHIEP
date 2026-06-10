package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Vai_tro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_vai_tro", nullable = false, length = 100)
    private String tenVaiTro;
}
