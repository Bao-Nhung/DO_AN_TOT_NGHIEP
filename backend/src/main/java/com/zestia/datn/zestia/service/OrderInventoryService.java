package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class OrderInventoryService {

    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final HoaDonRepository hoaDonRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;
    private final GiamGiaRepository giamGiaRepo;
    private final InventoryMovementService inventoryMovementService;

    @Transactional
    public void restoreReservation(HoaDon order) {
        if (order == null || order.getId() == null) return;
        if (Boolean.TRUE.equals(order.getDaHoanTonKho())) return;
        restoreStock(order);
        restoreVoucher(order);
        order.setDaHoanTonKho(true);
        hoaDonRepo.save(order);
    }

    private void restoreStock(HoaDon order) {
        List<HoaDonChiTiet> details = hoaDonChiTietRepo.findByHoaDonId(order.getId()).stream()
                .sorted(Comparator.comparing(detail -> detail.getSanPhamChiTiet() != null
                        && detail.getSanPhamChiTiet().getId() != null
                                ? detail.getSanPhamChiTiet().getId()
                                : Integer.MAX_VALUE))
                .toList();
        for (HoaDonChiTiet detail : details) {
            if (detail.getSanPhamChiTiet() == null || detail.getSanPhamChiTiet().getId() == null || detail.getSoLuong() == null) {
                continue;
            }
            sanPhamChiTietRepo.findByIdForUpdate(detail.getSanPhamChiTiet().getId()).ifPresent(variant -> {
                int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
                int afterStock = currentStock + detail.getSoLuong();
                variant.setSoLuong(afterStock);
                sanPhamChiTietRepo.save(variant);
                inventoryMovementService.record(
                        variant, currentStock, afterStock, "HOAN_DON",
                        order.getMaHoaDon(), "System",
                        "Hoàn tồn do hủy, giao thất bại hoặc thanh toán thất bại"
                );
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
