package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.DotKhuyenMai;
import com.zestia.datn.zestia.entity.PhamViKhuyenMai;
import com.zestia.datn.zestia.entity.SanPhamChiTiet;
import com.zestia.datn.zestia.repository.DotKhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PromotionPricingService {
    private static final long CACHE_MILLIS = 15_000;
    private final DotKhuyenMaiRepository campaignRepo;
    private volatile List<DotKhuyenMai> cachedCampaigns = List.of();
    private volatile long cacheExpiresAt;

    @Transactional(readOnly = true)
    public List<DotKhuyenMai> activeCampaigns() {
        long nowMillis = System.currentTimeMillis();
        List<DotKhuyenMai> local = cachedCampaigns;
        if (nowMillis < cacheExpiresAt) return local;
        synchronized (this) {
            if (nowMillis >= cacheExpiresAt) {
                cachedCampaigns = List.copyOf(campaignRepo.findActiveAt(LocalDateTime.now()));
                cacheExpiresAt = nowMillis + CACHE_MILLIS;
            }
            return cachedCampaigns;
        }
    }

    public PriceQuote quote(SanPhamChiTiet variant) {
        return quote(variant, activeCampaigns());
    }

    public PriceQuote quote(SanPhamChiTiet variant, List<DotKhuyenMai> campaigns) {
        BigDecimal basePrice = variant != null && variant.getGiaBan() != null
                ? variant.getGiaBan() : BigDecimal.ZERO;
        PriceQuote best = new PriceQuote(basePrice, basePrice, BigDecimal.ZERO, null, null, null);
        if (variant == null || campaigns == null) return best;

        for (DotKhuyenMai campaign : campaigns) {
            if (!matches(campaign, variant)) continue;
            BigDecimal discount = discount(campaign, basePrice);
            BigDecimal effective = basePrice.subtract(discount).max(BigDecimal.ZERO);
            if (effective.compareTo(best.effectivePrice()) < 0) {
                best = new PriceQuote(
                        basePrice,
                        effective,
                        discount,
                        campaign.getId(),
                        campaign.getMaDot(),
                        campaign.getTenDot()
                );
            }
        }
        return best;
    }

    public void invalidateCache() {
        cacheExpiresAt = 0;
        cachedCampaigns = List.of();
    }

    private boolean matches(DotKhuyenMai campaign, SanPhamChiTiet variant) {
        List<PhamViKhuyenMai> scopes = campaign.getPhamVis();
        if (scopes == null || scopes.isEmpty()) return true;
        return scopes.stream().anyMatch(scope -> matches(scope, variant));
    }

    private boolean matches(PhamViKhuyenMai scope, SanPhamChiTiet variant) {
        if (scope.getSanPham() != null && !sameId(scope.getSanPham().getId(), variant.getSanPham() != null ? variant.getSanPham().getId() : null)) return false;
        if (scope.getLoaiSanPham() != null && !sameId(scope.getLoaiSanPham().getId(),
                variant.getSanPham() != null && variant.getSanPham().getLoaiSanPham() != null ? variant.getSanPham().getLoaiSanPham().getId() : null)) return false;
        if (scope.getMauSac() != null && !sameId(scope.getMauSac().getId(), variant.getMauSac() != null ? variant.getMauSac().getId() : null)) return false;
        if (scope.getKichThuoc() != null && !sameId(scope.getKichThuoc().getId(), variant.getKichThuoc() != null ? variant.getKichThuoc().getId() : null)) return false;
        return true;
    }

    private BigDecimal discount(DotKhuyenMai campaign, BigDecimal price) {
        BigDecimal value = campaign.getGiaTriGiam() != null ? campaign.getGiaTriGiam() : BigDecimal.ZERO;
        BigDecimal result = "PERCENT".equalsIgnoreCase(campaign.getLoaiGiam())
                ? price.multiply(value).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : value;
        return result.max(BigDecimal.ZERO).min(price);
    }

    private boolean sameId(Integer left, Integer right) {
        return Objects.equals(left, right);
    }

    public record PriceQuote(BigDecimal basePrice, BigDecimal effectivePrice, BigDecimal discount,
                             Integer campaignId, String campaignCode, String campaignName) {
        public boolean discounted() {
            return campaignId != null && discount != null && discount.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal discountPercent() {
            if (!discounted() || basePrice.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
            return discount.multiply(BigDecimal.valueOf(100))
                    .divide(basePrice, 2, RoundingMode.HALF_UP);
        }
    }
}
