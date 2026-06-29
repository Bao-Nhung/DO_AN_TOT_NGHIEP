package com.zestia.datn.zestia.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTrackingDTO {

    private Integer id;
    private String maHoaDon;
    private String trangThaiTracking; // pending, processing, shipped, delivered, cancelled
    private LocalDateTime ngayGiaoHangDuKien;
    private LocalDateTime ngayGiaoHangThucTe;
    private String diaChiGiaoHang;
    private BigDecimal tongTien;
    private String hinhThucThanhToan;

    private CustomerInfoDTO khachHang;
    private List<OrderItemDTO> chiTiets;
    private List<TrackingHistoryDTO> trackingHistory;
    private LocalDateTime ngayTao;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerInfoDTO {
        private String hoVaTen;
        private String soDienThoai;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private Integer id;
        private String tenVay;
        private String mauSac;
        private String maHex;
        private String kichThuoc;
        private String anhUrl;
        private Integer soLuong;
        private BigDecimal donGia;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackingHistoryDTO {
        private Integer id;
        private String trangThai;
        private String moTa;
        private LocalDateTime ngayCapNhat;
    }
}