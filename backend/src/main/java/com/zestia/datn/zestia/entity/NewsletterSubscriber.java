package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Newsletter_subscriber",
        uniqueConstraints = @UniqueConstraint(name = "UQ_Newsletter_subscriber_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(name = "ngay_dang_ky")
    private LocalDateTime ngayDangKy;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
}
