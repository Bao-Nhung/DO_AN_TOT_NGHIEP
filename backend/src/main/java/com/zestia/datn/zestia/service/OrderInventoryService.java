package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderInventoryService {

    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final VayChiTietRepository vayChiTietRepo;
    private final GiamGiaRepository giamGiaRepo;

    @Transactional
    public void restoreReservation(HoaDon order) {
        if (order == null || order.getId() == null) return;
        restoreStock(order);
        restoreVoucher(order);
    }

    private void restoreStock(HoaDon order) {
        List<HoaDonChiTiet> details = hoaDonChiTietRepo.findByHoaDonId(order.getId());
        for (HoaDonChiTiet detail : details) {
            if (detail.getVayChiTiet() == null || detail.getVayChiTiet().getId() == null || detail.getSoLuong() == null) {
                continue;
            }
            vayChiTietRepo.findByIdForUpdate(detail.getVayChiTiet().getId()).ifPresent(variant -> {
                int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
                variant.setSoLuong(currentStock + detail.getSoLuong());
                vayChiTietRepo.save(variant);
            });
        }
    }

    private void restoreVoucher(HoaDon order) {
        GiamGia voucher = order.getGiamGia();
        if (voucher == null || voucher.getId() == null) return;
        giamGiaRepo.findByIdForUpdate(voucher.getId()).ifPresent(locked -> {
            if (locked.getSoLuong() == null) return;
            int current = locked.getSoLuong() != null ? locked.getSoLuong() : 0;
            locked.setSoLuong(current + 1);
            giamGiaRepo.save(locked);
        });
    }
}
