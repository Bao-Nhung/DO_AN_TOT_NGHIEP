package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    public static final String TIER_BRONZE = "Đồng";
    public static final String TIER_SILVER = "Bạc";
    public static final String TIER_GOLD = "Vàng";
    public static final String TIER_DIAMOND = "Kim Cương";

    private static final BigDecimal THRESHOLD_SILVER = new BigDecimal("5000000");
    private static final BigDecimal THRESHOLD_GOLD = new BigDecimal("15000000");
    private static final BigDecimal THRESHOLD_DIAMOND = new BigDecimal("30000000");

    private final KhachHangRepository khachHangRepo;
    private final HoaDonRepository hoaDonRepo;
    private final LichSuThanhToanRepository paymentHistoryRepo;

    public String calculateTier(BigDecimal totalSpent) {
        if (totalSpent == null) return TIER_BRONZE;
        if (totalSpent.compareTo(THRESHOLD_DIAMOND) >= 0) return TIER_DIAMOND;
        if (totalSpent.compareTo(THRESHOLD_GOLD) >= 0) return TIER_GOLD;
        if (totalSpent.compareTo(THRESHOLD_SILVER) >= 0) return TIER_SILVER;
        return TIER_BRONZE;
    }

    public BigDecimal getNextTierThreshold(String tier) {
        if (tier == null) return THRESHOLD_SILVER;
        return switch (tier) {
            case TIER_BRONZE -> THRESHOLD_SILVER;
            case TIER_SILVER -> THRESHOLD_GOLD;
            case TIER_GOLD -> THRESHOLD_DIAMOND;
            default -> THRESHOLD_DIAMOND;
        };
    }

    @Transactional
    public void earnPointsOnOrderCompletion(HoaDon order) {
        if (order == null || order.getKhachHang() == null || order.getKhachHang().getId() == null) return;
        recalculate(order.getKhachHang().getId());
    }

    @Transactional
    public void recalculate(Integer customerId) {
        if (customerId == null) return;
        KhachHang customer = khachHangRepo.findById(customerId).orElse(null);
        if (customer == null) return;
        BigDecimal completedSpend = Objects.requireNonNullElse(
                hoaDonRepo.sumCompletedSpendByCustomerId(customerId), BigDecimal.ZERO);
        BigDecimal refundAdjustments = Objects.requireNonNullElse(
                paymentHistoryRepo.sumRefundAdjustmentsForCompletedOrders(customerId), BigDecimal.ZERO);
        BigDecimal netSpend = completedSpend.add(refundAdjustments).max(BigDecimal.ZERO);
        int points = netSpend.divide(new BigDecimal("100000"), 0, RoundingMode.FLOOR).intValue();
        customer.setDiemTichLuy(Math.max(points, 0));
        customer.setTongChiTieu(netSpend);
        customer.setHangThanhVien(calculateTier(netSpend));
        khachHangRepo.save(customer);
        log.info("Đã đồng bộ thành viên #{}: {} điểm, tổng chi tiêu {}, hạng {}",
                customerId, customer.getDiemTichLuy(), netSpend, customer.getHangThanhVien());
    }

    public Map<String, Object> toLoyaltySummaryMap(KhachHang customer) {
        int points = customer != null && customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0;
        BigDecimal spent = customer != null && customer.getTongChiTieu() != null ? customer.getTongChiTieu() : BigDecimal.ZERO;
        String tier = customer != null && customer.getHangThanhVien() != null ? customer.getHangThanhVien() : TIER_BRONZE;
        BigDecimal nextThreshold = getNextTierThreshold(tier);
        BigDecimal neededForNext = nextThreshold.subtract(spent);
        if (neededForNext.compareTo(BigDecimal.ZERO) < 0) neededForNext = BigDecimal.ZERO;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("diemTichLuy", points);
        map.put("tongChiTieu", spent);
        map.put("hangThanhVien", tier);
        map.put("mocChiTieuKeTiep", nextThreshold);
        map.put("canChiTieuThem", neededForNext);
        return map;
    }
}
