package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.*;
import com.zestia.datn.zestia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionManagementService {
    private final DotKhuyenMaiRepository campaignRepo;
    private final VayRepository productRepo;
    private final LoaiVayRepository categoryRepo;
    private final MauSacRepository colorRepo;
    private final KichThuocRepository sizeRepo;
    private final PromotionPricingService pricingService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        return campaignRepo.findAllDetailed().stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(Integer id) {
        return toMap(requireDetailed(id));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        DotKhuyenMai campaign = new DotKhuyenMai();
        campaign.setNgayTao(LocalDateTime.now());
        campaign.setPhamVis(new ArrayList<>());
        apply(campaign, body, false);
        DotKhuyenMai saved = campaignRepo.save(campaign);
        pricingService.invalidateCache();
        return toMap(saved);
    }

    @Transactional
    public Map<String, Object> update(Integer id, Map<String, Object> body) {
        DotKhuyenMai campaign = requireDetailed(id);
        apply(campaign, body, true);
        DotKhuyenMai saved = campaignRepo.save(campaign);
        pricingService.invalidateCache();
        return toMap(saved);
    }

    @Transactional
    public void delete(Integer id) {
        DotKhuyenMai campaign = requireDetailed(id);
        campaignRepo.delete(campaign);
        pricingService.invalidateCache();
    }

    private void apply(DotKhuyenMai campaign, Map<String, Object> body, boolean updating) {
        String code = clean(body.get("maDot"));
        if (code == null) code = autoCode();
        code = code.toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_-]{3,50}")) {
            throw badRequest("Mã đợt chỉ gồm chữ in hoa, số, gạch ngang hoặc gạch dưới");
        }
        if ((!updating && campaignRepo.existsByMaDotIgnoreCase(code))
                || (updating && campaignRepo.existsByMaDotIgnoreCaseAndIdNot(code, campaign.getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã đợt khuyến mãi đã tồn tại");
        }

        String name = required(body.get("tenDot"), "Tên đợt khuyến mãi", 150);
        String type = required(body.get("loaiGiam"), "Loại giảm", 20).toUpperCase(Locale.ROOT);
        if (!Set.of("PERCENT", "FIXED").contains(type)) throw badRequest("Loại giảm không hợp lệ");
        BigDecimal value = decimal(body.get("giaTriGiam"));
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) throw badRequest("Giá trị giảm phải lớn hơn 0");
        if ("PERCENT".equals(type) && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw badRequest("Phần trăm giảm không được vượt quá 100%");
        }

        LocalDateTime start = dateTime(body.get("ngayBatDau"), "Ngày bắt đầu");
        LocalDateTime end = dateTime(body.get("ngayKetThuc"), "Ngày kết thúc");
        if (!end.isAfter(start)) throw badRequest("Ngày kết thúc phải sau ngày bắt đầu");

        campaign.setMaDot(code);
        campaign.setTenDot(name);
        campaign.setLoaiGiam(type);
        campaign.setGiaTriGiam(value);
        campaign.setNgayBatDau(start);
        campaign.setNgayKetThuc(end);
        campaign.setTrangThai(byteValue(body.get("trangThai"), (byte) 1));
        campaign.setDoUuTien(intValue(body.get("doUuTien"), 0));

        campaign.getPhamVis().clear();
        Object rawScopes = body.get("phamVis");
        if (rawScopes instanceof List<?> scopes && !scopes.isEmpty()) {
            Set<String> unique = new HashSet<>();
            for (Object rawScope : scopes) {
                if (!(rawScope instanceof Map<?, ?> scope)) continue;
                Integer productId = intValue(scope.get("idVay"), null);
                Integer categoryId = intValue(scope.get("idLoaiVay"), null);
                Integer colorId = intValue(scope.get("idMauSac"), null);
                Integer sizeId = intValue(scope.get("idKichThuoc"), null);
                String key = productId + ":" + categoryId + ":" + colorId + ":" + sizeId;
                if (!unique.add(key)) continue;
                campaign.getPhamVis().add(scope(campaign, productId, categoryId, colorId, sizeId));
            }
        }
        if (campaign.getPhamVis().isEmpty()) {
            campaign.getPhamVis().add(PhamViKhuyenMai.builder().dotKhuyenMai(campaign).build());
        }
    }

    private PhamViKhuyenMai scope(DotKhuyenMai campaign, Integer productId, Integer categoryId,
                                  Integer colorId, Integer sizeId) {
        Vay product = productId == null ? null : productRepo.findById(productId)
                .orElseThrow(() -> badRequest("Sản phẩm trong phạm vi không tồn tại"));
        LoaiVay category = categoryId == null ? null : categoryRepo.findById(categoryId)
                .orElseThrow(() -> badRequest("Loại sản phẩm trong phạm vi không tồn tại"));
        MauSac color = colorId == null ? null : colorRepo.findById(colorId)
                .orElseThrow(() -> badRequest("Màu trong phạm vi không tồn tại"));
        KichThuoc size = sizeId == null ? null : sizeRepo.findById(sizeId)
                .orElseThrow(() -> badRequest("Kích thước trong phạm vi không tồn tại"));
        return PhamViKhuyenMai.builder()
                .dotKhuyenMai(campaign)
                .vay(product)
                .loaiVay(category)
                .mauSac(color)
                .kichThuoc(size)
                .build();
    }

    private DotKhuyenMai requireDetailed(Integer id) {
        return campaignRepo.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đợt khuyến mãi"));
    }

    private Map<String, Object> toMap(DotKhuyenMai campaign) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", campaign.getId());
        map.put("maDot", campaign.getMaDot());
        map.put("tenDot", campaign.getTenDot());
        map.put("loaiGiam", campaign.getLoaiGiam());
        map.put("giaTriGiam", campaign.getGiaTriGiam());
        map.put("ngayBatDau", campaign.getNgayBatDau());
        map.put("ngayKetThuc", campaign.getNgayKetThuc());
        map.put("trangThai", campaign.getTrangThai());
        map.put("doUuTien", campaign.getDoUuTien());
        map.put("ngayTao", campaign.getNgayTao());
        map.put("phamVis", campaign.getPhamVis().stream().map(this::scopeMap).toList());
        map.put("trangThaiHienTai", currentStatus(campaign));
        return map;
    }

    private Map<String, Object> scopeMap(PhamViKhuyenMai scope) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", scope.getId());
        map.put("idVay", scope.getVay() != null ? scope.getVay().getId() : null);
        map.put("tenVay", scope.getVay() != null ? scope.getVay().getTenVay() : null);
        map.put("idLoaiVay", scope.getLoaiVay() != null ? scope.getLoaiVay().getId() : null);
        map.put("tenLoaiVay", scope.getLoaiVay() != null ? scope.getLoaiVay().getTenLoaiVay() : null);
        map.put("idMauSac", scope.getMauSac() != null ? scope.getMauSac().getId() : null);
        map.put("tenMauSac", scope.getMauSac() != null ? scope.getMauSac().getTenMauSac() : null);
        map.put("idKichThuoc", scope.getKichThuoc() != null ? scope.getKichThuoc().getId() : null);
        map.put("tenKichThuoc", scope.getKichThuoc() != null ? scope.getKichThuoc().getTenKichThuoc() : null);
        return map;
    }

    private String currentStatus(DotKhuyenMai campaign) {
        if (!Byte.valueOf((byte) 1).equals(campaign.getTrangThai())) return "INACTIVE";
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(campaign.getNgayBatDau())) return "UPCOMING";
        if (now.isAfter(campaign.getNgayKetThuc())) return "ENDED";
        return "ACTIVE";
    }

    private String autoCode() {
        return "KM" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String required(Object value, String label, int maxLength) {
        String text = clean(value);
        if (text == null) throw badRequest("Vui lòng nhập " + label.toLowerCase(Locale.ROOT));
        if (text.length() > maxLength) throw badRequest(label + " không được vượt quá " + maxLength + " ký tự");
        return text;
    }

    private String clean(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private BigDecimal decimal(Object value) {
        try { return value == null ? null : new BigDecimal(String.valueOf(value)); }
        catch (Exception e) { return null; }
    }

    private LocalDateTime dateTime(Object value, String label) {
        try { return LocalDateTime.parse(required(value, label, 40)); }
        catch (DateTimeParseException e) { throw badRequest(label + " không hợp lệ"); }
    }

    private Integer intValue(Object value, Integer fallback) {
        try { return value == null || String.valueOf(value).isBlank() ? fallback : Integer.valueOf(String.valueOf(value)); }
        catch (Exception e) { return fallback; }
    }

    private Byte byteValue(Object value, byte fallback) {
        Integer parsed = intValue(value, (int) fallback);
        return (byte) (parsed != null && parsed == 1 ? 1 : 0);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
