package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.Vay;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.repository.VayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final KhachHangRepository khachHangRepo;
    private final VayRepository vayRepo;
    private final VayChiTietRepository vayCtRepo;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<HoaDon> allOrders = hoaDonRepo.findAll();
        List<HoaDon> revenueOrders = allOrders.stream()
                .filter(this::isRevenueOrder)
                .toList();

        BigDecimal doanhThu = revenueOrders.stream()
                .map(o -> o.getTongTien() != null ? o.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("doanhThu", doanhThu);
        map.put("tongDonHang", allOrders.size());
        map.put("tongKhachHang", khachHangRepo.count());
        map.put("tongSanPham", vayRepo.count());
        map.put("tongBienThe", vayCtRepo.count());
        map.put("tongLoaiVay", vayRepo.count());
        map.put("topSellingProducts", topSellingProducts(revenueOrders));
        map.put("lowStockVariants", lowStockVariants());
        return map;
    }

    private boolean isRevenueOrder(HoaDon order) {
        if (order == null || order.getTrangThai() == null) return false;
        byte status = order.getTrangThai();
        if (status == 5 || status == 6 || status == 7 || status == 9) return false;
        return status == 4 || Boolean.TRUE.equals(order.getDaThanhToan());
    }

    private List<Map<String, Object>> topSellingProducts(List<HoaDon> orders) {
        Map<Integer, ProductSales> salesByProduct = new LinkedHashMap<>();
        for (HoaDon order : orders) {
            for (HoaDonChiTiet detail : hoaDonCtRepo.findByHoaDonId(order.getId())) {
                VayChiTiet variant = detail.getVayChiTiet();
                if (variant == null || variant.getVay() == null || detail.getSoLuong() == null) continue;
                Vay product = variant.getVay();
                ProductSales sales = salesByProduct.computeIfAbsent(product.getId(), id -> new ProductSales(product));
                BigDecimal unitPrice = detail.getDonGia() != null ? detail.getDonGia() : BigDecimal.ZERO;
                sales.quantity += detail.getSoLuong();
                sales.revenue = sales.revenue.add(unitPrice.multiply(BigDecimal.valueOf(detail.getSoLuong())));
            }
        }

        return salesByProduct.values().stream()
                .sorted(Comparator.comparingInt(ProductSales::quantity).reversed())
                .limit(5)
                .map(ProductSales::toMap)
                .toList();
    }

    private List<Map<String, Object>> lowStockVariants() {
        return vayCtRepo.findByTrangThai((byte) 1).stream()
                .filter(this::activeVariant)
                .filter(v -> (v.getSoLuong() != null ? v.getSoLuong() : 0) <= 5)
                .sorted(Comparator.comparingInt(v -> v.getSoLuong() != null ? v.getSoLuong() : 0))
                .limit(8)
                .map(v -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("variantId", v.getId());
                    item.put("productId", v.getVay().getId());
                    item.put("tenVay", v.getVay().getTenVay());
                    item.put("maVay", v.getVay().getMaVay());
                    item.put("mauSac", v.getMauSac() != null ? v.getMauSac().getTenMauSac() : null);
                    item.put("kichThuoc", v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : null);
                    item.put("soLuong", v.getSoLuong() != null ? v.getSoLuong() : 0);
                    return item;
                })
                .toList();
    }

    private boolean activeVariant(VayChiTiet variant) {
        return variant != null
                && variant.getVay() != null
                && variant.getVay().getTrangThai() != null
                && variant.getVay().getTrangThai() == 1;
    }

    private static class ProductSales {
        private final Vay product;
        private int quantity;
        private BigDecimal revenue = BigDecimal.ZERO;

        private ProductSales(Vay product) {
            this.product = product;
        }

        private int quantity() {
            return quantity;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", product.getId());
            map.put("maVay", product.getMaVay());
            map.put("tenVay", product.getTenVay());
            map.put("soLuongBan", quantity);
            map.put("doanhThu", revenue);
            return map;
        }
    }
}
