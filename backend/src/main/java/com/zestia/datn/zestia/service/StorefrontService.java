package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.ChinhSachCuaHang;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StorefrontService {
    private final SanPhamRepository productRepo;
    private final LoaiSanPhamRepository categoryRepo;
    private final KhachHangRepository customerRepo;
    private final HoaDonRepository orderRepo;
    private final HoaDonChiTietRepository orderDetailRepo;
    private final DanhGiaRepository reviewRepo;
    private final ChinhSachCuaHangRepository policyRepo;

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        DanhGiaRepository.ReviewSummary reviewSummary = reviewRepo.summarizeStore();
        Number average = reviewSummary != null && reviewSummary.getAverage() != null ? reviewSummary.getAverage() : 0;
        Number reviewCount = reviewSummary != null && reviewSummary.getTotal() != null ? reviewSummary.getTotal() : 0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("activeProductCount", productRepo.countByTrangThai((byte) 1));
        response.put("categoryCount", categoryRepo.count());
        response.put("customerCount", customerRepo.count());
        response.put("completedOrderCount", orderRepo.countByTrangThai((byte) 4));
        response.put("averageRating", BigDecimal.valueOf(average.doubleValue()).setScale(1, RoundingMode.HALF_UP));
        response.put("reviewCount", reviewCount.longValue());
        response.put("bestsellers", bestsellers());
        response.put("reviewHighlights", reviewHighlights());
        response.put("policies", activePolicies());
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> policies() {
        return activePolicies();
    }

    private List<Map<String, Object>> bestsellers() {
        return orderDetailRepo.findTopSellingProducts(PageRequest.of(0, 8)).stream()
                .map(row -> Map.<String, Object>of(
                        "productId", ((Number) row[0]).intValue(),
                        "soldQuantity", ((Number) row[1]).longValue()
                ))
                .toList();
    }

    private List<Map<String, Object>> reviewHighlights() {
        return reviewRepo.findReviewHighlights(PageRequest.of(0, 6)).stream()
                .map(this::reviewMap)
                .toList();
    }

    private Map<String, Object> reviewMap(DanhGiaRepository.ReviewHighlight review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("productId", review.getProductId());
        map.put("productName", review.getProductName());
        map.put("customerName", CustomerPrivacy.maskName(review.getCustomerName()));
        map.put("stars", review.getStars());
        map.put("content", review.getContent());
        map.put("createdAt", review.getCreatedAt());
        return map;
    }

    private List<Map<String, Object>> activePolicies() {
        return policyRepo.findByTrangThaiOrderByThuTuAsc((byte) 1).stream()
                .map(this::policyMap)
                .toList();
    }

    private Map<String, Object> policyMap(ChinhSachCuaHang policy) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", policy.getMaChinhSach());
        map.put("title", policy.getTieuDe());
        map.put("summary", policy.getTomTat());
        map.put("content", policy.getNoiDung());
        map.put("numericValue", policy.getGiaTriSo());
        map.put("unit", policy.getDonVi());
        map.put("updatedAt", policy.getNgayCapNhat());
        return map;
    }
}
