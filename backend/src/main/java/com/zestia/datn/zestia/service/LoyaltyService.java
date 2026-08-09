package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public String calculateTier(BigDecimal totalSpent) {
        if (totalSpent == null) return TIER_BRONZE;
        if (totalSpent.compareTo(THRESHOLD_DIAMOND) >= 0) return TIER_DIAMOND;
        if (totalSpent.compareTo(THRESHOLD_GOLD) >= 0) return TIER_GOLD;
        if (totalSpent.compareTo(THRESHOLD_SILVER) >= 0) return TIER_SILVER;
        return TIER_BRONZE;
    }

    public BigDecimal getTierDiscountPercent(String tier) {
        if (tier == null) return BigDecimal.ZERO;
        return switch (tier) {
            case TIER_SILVER -> new BigDecimal("2");
            case TIER_GOLD -> new BigDecimal("5");
            case TIER_DIAMOND -> new BigDecimal("10");
            default -> BigDecimal.ZERO;
        };
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
        Integer customerId = order.getKhachHang().getId();
        KhachHang customer = khachHangRepo.findById(customerId).orElse(null);
        if (customer == null) return;

        BigDecimal paidAmount = order.getTongTien() != null ? order.getTongTien() : BigDecimal.ZERO;
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        // Tích điểm: 1% giá trị đơn hàng (1 điểm = 1.000 VNĐ -> số điểm = tổng tiền / 100.000)
        int pointsEarned = paidAmount.divide(new BigDecimal("100000"), 0, RoundingMode.FLOOR).intValue();
        if (pointsEarned < 0) pointsEarned = 0;

        int currentPoints = customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0;
        customer.setDiemTichLuy(currentPoints + pointsEarned);

        BigDecimal currentSpent = customer.getTongChiTieu() != null ? customer.getTongChiTieu() : BigDecimal.ZERO;
        BigDecimal newSpent = currentSpent.add(paidAmount);
        customer.setTongChiTieu(newSpent);

        String newTier = calculateTier(newSpent);
        customer.setHangThanhVien(newTier);

        khachHangRepo.save(customer);
        log.info("Tích điểm thành công cho khách hàng #{}: +{} điểm, Tổng tích lũy: {}, Hạng: {}",
                customerId, pointsEarned, newSpent, newTier);
    }

    @Transactional
    public boolean redeemPoints(KhachHang customer, int pointsToRedeem) {
        if (customer == null || pointsToRedeem <= 0) return false;
        int currentPoints = customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0;
        if (currentPoints < pointsToRedeem) return false;

        customer.setDiemTichLuy(currentPoints - pointsToRedeem);
        khachHangRepo.save(customer);
        return true;
    }

    @Transactional
    public void restorePoints(KhachHang customer, int pointsToRestore) {
        if (customer == null || pointsToRestore <= 0) return;
        int currentPoints = customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0;
        customer.setDiemTichLuy(currentPoints + pointsToRestore);
        khachHangRepo.save(customer);
    }

    public Map<String, Object> toLoyaltySummaryMap(KhachHang customer) {
        int points = customer != null && customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0;
        BigDecimal spent = customer != null && customer.getTongChiTieu() != null ? customer.getTongChiTieu() : BigDecimal.ZERO;
        String tier = customer != null && customer.getHangThanhVien() != null ? customer.getHangThanhVien() : TIER_BRONZE;
        BigDecimal discountPercent = getTierDiscountPercent(tier);
        BigDecimal nextThreshold = getNextTierThreshold(tier);
        BigDecimal neededForNext = nextThreshold.subtract(spent);
        if (neededForNext.compareTo(BigDecimal.ZERO) < 0) neededForNext = BigDecimal.ZERO;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("diemTichLuy", points);
        map.put("giaTriDiemQuyDoi", new BigDecimal(points).multiply(new BigDecimal("1000")));
        map.put("tongChiTieu", spent);
        map.put("hangThanhVien", tier);
        map.put("chietKhauPhanTram", discountPercent);
        map.put("mocChiTieuKeTiep", nextThreshold);
        map.put("canChiTieuThem", neededForNext);
        return map;
    }
}
