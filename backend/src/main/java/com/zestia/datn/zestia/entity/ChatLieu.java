package com.zestia.datn.zestia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Chat_lieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_chat_lieu", nullable = false, length = 100)
    private String tenChatLieu;

    @Column(name = "trang_thai")
    private Byte trangThai;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
