package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ai_visual_search_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiVisualSearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "image_filename", length = 255)
    private String imageFilename;

    @Column(name = "detected_category", length = 100)
    private String detectedCategory;

    @Column(name = "detected_color", length = 100)
    private String detectedColor;

    @Column(name = "matched_product_ids", length = 500)
    private String matchedProductIds;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
