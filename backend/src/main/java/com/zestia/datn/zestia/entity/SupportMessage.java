package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Tin_nhan_ho_tro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ho_tro_chat", nullable = false)
    private SupportConversation conversation;

    @Column(name = "loai_nguoi_gui", nullable = false, length = 20)
    private String senderType;

    @Column(name = "ten_nguoi_gui", length = 150)
    private String senderName;

    @Column(name = "noi_dung", nullable = false, length = 1000)
    private String content;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime createdAt;
}
