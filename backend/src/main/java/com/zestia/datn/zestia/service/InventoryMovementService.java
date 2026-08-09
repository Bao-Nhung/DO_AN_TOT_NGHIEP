package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.BienDongTonKho;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.BienDongTonKhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {
    private final BienDongTonKhoRepository movementRepo;

    public void record(SanPhamChiTiet variant, int before, int after, String type,
                       String reference, String actor, String note) {
        if (variant == null || variant.getId() == null || before == after) return;
        movementRepo.save(BienDongTonKho.builder()
                .sanPhamChiTiet(variant)
                .soLuongTruoc(before)
                .soLuongThayDoi(after - before)
                .soLuongSau(after)
                .loaiBienDong(type)
                .maThamChieu(trim(reference, 100))
                .nguoiThucHien(trim(actor, 150))
                .ghiChu(trim(note, 500))
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private String trim(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim();
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max);
    }
}
