package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.LuotQuayMayMan;
import com.zestia.datn.zestia.entity.PhanThuongVongQuay;
import com.zestia.datn.zestia.entity.VongQuayMayMan;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LuotQuayMayManRepository;
import com.zestia.datn.zestia.repository.PhanThuongVongQuayRepository;
import com.zestia.datn.zestia.repository.VongQuayMayManRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LuckyWheelService {
    private static final byte COMPLETED_ORDER = 4;
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern ICON = Pattern.compile("^bi-[a-z0-9-]{2,45}$");
    private static final Pattern CUSTOM_ICON = Pattern.compile("^/images/lucky-wheel/[A-Za-z0-9._-]{1,180}$");
    private static final Set<String> CLAIM_STATUSES = Set.of(
            LuotQuayMayMan.WAITING, LuotQuayMayMan.DELIVERED, LuotQuayMayMan.NO_PRIZE
    );

    private final VongQuayMayManRepository campaignRepository;
    private final PhanThuongVongQuayRepository prizeRepository;
    private final LuotQuayMayManRepository spinRepository;
    private final HoaDonRepository orderRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public Map<String, Object> activeCampaign() {
        VongQuayMayMan campaign = currentCampaign();
        if (campaign == null) return Map.of("active", false);
        List<PhanThuongVongQuay> prizes = prizeRepository.findByChienDichIdOrderByThuTuAscIdAsc(campaign.getId())
                .stream().filter(this::canAppear).toList();
        if (prizes.isEmpty()) return Map.of("active", false);

        Map<String, Object> result = campaignSummary(campaign);
        result.put("active", true);
        result.put("segments", prizes.stream().map(this::publicPrize).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkEligibility(String orderCode, String phone) {
        VongQuayMayMan campaign = currentCampaign();
        if (campaign == null) return eligibility(false, "Hiện chưa có vòng quay đang diễn ra.", null, null);

        String normalizedCode = normalizeOrderCode(orderCode);
        String normalizedPhone = normalizePhone(phone);
        HoaDon order = orderRepository.findOrderByCodeAndPhone(normalizedCode, normalizedPhone).orElse(null);
        if (order == null) return eligibility(false, "Mã đơn hoặc số điện thoại chưa đúng.", campaign, null);

        String reason = ineligibleReason(order, campaign);
        if (reason != null) return eligibility(false, reason, campaign, order);

        Optional<LuotQuayMayMan> existing = spinRepository
                .findByChienDichIdAndMaHoaDonIgnoreCase(campaign.getId(), normalizedCode);
        if (existing.isPresent()) return existingSpin(existing.get(), true);

        return eligibility(true, "Đơn hàng đủ điều kiện nhận một lượt quay.", campaign, order);
    }

    @Transactional
    public Map<String, Object> spin(String orderCode, String phone) {
        VongQuayMayMan campaign = currentCampaign();
        if (campaign == null) throw badRequest("Hiện chưa có vòng quay đang diễn ra.");

        String normalizedCode = normalizeOrderCode(orderCode);
        String normalizedPhone = normalizePhone(phone);
        HoaDon order = orderRepository.findByMaHoaDonForUpdate(normalizedCode)
                .orElseThrow(() -> badRequest("Mã đơn hoặc số điện thoại chưa đúng."));
        if (!phoneMatches(order, normalizedPhone)) throw badRequest("Mã đơn hoặc số điện thoại chưa đúng.");

        String reason = ineligibleReason(order, campaign);
        if (reason != null) throw badRequest(reason);

        Optional<LuotQuayMayMan> existing = spinRepository
                .findByChienDichIdAndMaHoaDonIgnoreCase(campaign.getId(), normalizedCode);
        if (existing.isPresent()) return existingSpin(existing.get(), true);

        List<PhanThuongVongQuay> prizes = prizeRepository.findActiveForUpdate(campaign.getId())
                .stream().filter(this::canAppear).toList();
        PhanThuongVongQuay selected = choosePrize(prizes);
        boolean won = PhanThuongVongQuay.PHYSICAL.equals(selected.getLoaiPhanThuong());
        if (won) {
            selected.setSoLuongCon(selected.getSoLuongCon() - 1);
            prizeRepository.save(selected);
        }

        LuotQuayMayMan spin = LuotQuayMayMan.builder()
                .chienDich(campaign)
                .phanThuong(selected)
                .maHoaDon(normalizedCode)
                .tenKhachHang(orderCustomerName(order))
                .soDienThoai(orderPhone(order))
                .emailKhachHang(orderEmail(order))
                .giaTriDon(defaultMoney(order.getTongTien()))
                .tenKetQua(selected.getTenPhanThuong())
                .trungThuong(won)
                .maNhanThuong(newClaimCode())
                .trangThaiNhan(won ? LuotQuayMayMan.WAITING : LuotQuayMayMan.NO_PRIZE)
                .ngayQuay(LocalDateTime.now())
                .build();
        return existingSpin(spinRepository.save(spin), false);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listCampaigns() {
        return campaignRepository.findAllLatest().stream().map(campaign -> {
            Map<String, Object> result = campaignSummary(campaign);
            result.put("trangThai", campaign.getTrangThai());
            result.put("ngayTao", campaign.getNgayTao());
            result.put("trangThaiHienTai", campaignStatus(campaign));
            result.put("tongLuotQuay", spinRepository.countByChienDichId(campaign.getId()));
            result.put("tongTrungThuong", spinRepository.countByChienDichIdAndTrungThuongTrue(campaign.getId()));
            result.put("choTrao", spinRepository.countByChienDichIdAndTrangThaiNhan(campaign.getId(), LuotQuayMayMan.WAITING));
            result.put("phanThuongs", prizeRepository.findByChienDichIdOrderByThuTuAscIdAsc(campaign.getId())
                    .stream().map(this::adminPrize).toList());
            return result;
        }).toList();
    }

    @Transactional
    public Map<String, Object> saveCampaign(Integer id, Map<String, Object> body) {
        if (body == null) throw badRequest("Dữ liệu chiến dịch không hợp lệ.");
        VongQuayMayMan campaign = id == null ? new VongQuayMayMan() : campaignRepository.findById(id)
                .orElseThrow(() -> notFound("Không tìm thấy chiến dịch vòng quay."));
        String code = string(body.get("maChienDich"));
        if (code == null || code.isBlank()) code = "VQ" + System.currentTimeMillis();
        code = code.trim().toUpperCase(Locale.ROOT);
        if ((id == null && campaignRepository.existsByMaChienDichIgnoreCase(code))
                || (id != null && campaignRepository.existsByMaChienDichIgnoreCaseAndIdNot(code, id))) {
            throw badRequest("Mã chiến dịch đã tồn tại.");
        }
        if (!code.matches("[A-Z0-9_-]{3,50}")) {
            throw badRequest("Mã chiến dịch chỉ gồm chữ in hoa, số, gạch ngang hoặc gạch dưới.");
        }

        String name = requiredText(body.get("tenChienDich"), "Tên chiến dịch", 3, 150);
        BigDecimal minimum = decimal(body.get("giaTriDonToiThieu"));
        if (minimum == null || minimum.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Giá trị đơn tối thiểu phải lớn hơn 0.");
        }
        LocalDateTime startsAt = dateTime(body.get("ngayBatDau"), "Thời gian bắt đầu");
        LocalDateTime endsAt = dateTime(body.get("ngayKetThuc"), "Thời gian kết thúc");
        if (!endsAt.isAfter(startsAt)) throw badRequest("Thời gian kết thúc phải sau thời gian bắt đầu.");
        byte status = byteValue(body.get("trangThai"), (byte) 1);
        if (status == 1 && campaignRepository.countActiveOverlaps(id, startsAt, endsAt) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Thời gian chiến dịch trùng với một vòng quay đang hoạt động khác.");
        }

        campaign.setMaChienDich(code);
        campaign.setTenChienDich(name);
        campaign.setMoTa(trimToLength(string(body.get("moTa")), 500));
        campaign.setGiaTriDonToiThieu(minimum);
        campaign.setNgayBatDau(startsAt);
        campaign.setNgayKetThuc(endsAt);
        campaign.setTrangThai(status);
        if (campaign.getNgayTao() == null) campaign.setNgayTao(LocalDateTime.now());
        VongQuayMayMan saved = campaignRepository.save(campaign);
        return campaignSummary(saved);
    }

    @Transactional
    public Map<String, Object> savePrize(Integer campaignId, Integer prizeId, Map<String, Object> body) {
        if (body == null) throw badRequest("Dữ liệu phần thưởng không hợp lệ.");
        VongQuayMayMan campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> notFound("Không tìm thấy chiến dịch vòng quay."));
        PhanThuongVongQuay prize = prizeId == null ? new PhanThuongVongQuay() : prizeRepository.findByIdForUpdate(prizeId)
                .orElseThrow(() -> notFound("Không tìm thấy phần thưởng."));
        if (prizeId != null && !Objects.equals(prize.getChienDich().getId(), campaignId)) {
            throw badRequest("Phần thưởng không thuộc chiến dịch này.");
        }

        String type = Optional.ofNullable(string(body.get("loaiPhanThuong")))
                .orElse(PhanThuongVongQuay.PHYSICAL).trim().toUpperCase(Locale.ROOT);
        if (!Set.of(PhanThuongVongQuay.PHYSICAL, PhanThuongVongQuay.NO_PRIZE).contains(type)) {
            throw badRequest("Loại phần thưởng không hợp lệ.");
        }
        if (prizeId != null && !type.equals(prize.getLoaiPhanThuong())
                && spinRepository.countByPhanThuongId(prizeId) > 0) {
            throw badRequest("Không thể đổi loại của phần thưởng đã phát sinh lượt quay.");
        }

        Integer weight = integer(body.get("trongSo"));
        if (weight == null || weight < 1 || weight > 100_000) throw badRequest("Trọng số phải từ 1 đến 100.000.");
        String color = Optional.ofNullable(string(body.get("mauHienThi"))).orElse("#D4564E").trim();
        if (!HEX_COLOR.matcher(color).matches()) throw badRequest("Màu hiển thị phải có dạng #RRGGBB.");
        String icon = Optional.ofNullable(string(body.get("bieuTuong"))).orElse("bi-gift").trim();
        if (!ICON.matcher(icon).matches()) throw badRequest("Biểu tượng phần thưởng không hợp lệ.");
        String customIcon = trimToLength(string(body.get("anhBieuTuong")), 500);
        if (customIcon != null && !customIcon.isBlank() && !CUSTOM_ICON.matcher(customIcon).matches()) {
            throw badRequest("Đường dẫn ảnh biểu tượng không hợp lệ.");
        }

        prize.setChienDich(campaign);
        prize.setTenPhanThuong(requiredText(body.get("tenPhanThuong"), "Tên phần thưởng", 2, 150));
        prize.setLoaiPhanThuong(type);
        prize.setTrongSo(weight);
        prize.setMauHienThi(color.toUpperCase(Locale.ROOT));
        prize.setBieuTuong(icon);
        prize.setAnhBieuTuong(customIcon == null || customIcon.isBlank() ? null : customIcon);
        prize.setThuTu(Optional.ofNullable(integer(body.get("thuTu"))).orElse(0));
        prize.setTrangThai(byteValue(body.get("trangThai"), (byte) 1));

        if (PhanThuongVongQuay.PHYSICAL.equals(type)) {
            Integer newQuantity = integer(body.get("soLuongBanDau"));
            if (newQuantity == null || newQuantity < 0) throw badRequest("Số lượng quà không được âm.");
            int deliveredOrReserved = prize.getSoLuongBanDau() == null || prize.getSoLuongCon() == null
                    ? 0 : Math.max(0, prize.getSoLuongBanDau() - prize.getSoLuongCon());
            if (newQuantity < deliveredOrReserved) throw badRequest("Số lượng mới nhỏ hơn số quà đã trúng.");
            prize.setSoLuongBanDau(newQuantity);
            prize.setSoLuongCon(newQuantity - deliveredOrReserved);
        } else {
            prize.setSoLuongBanDau(null);
            prize.setSoLuongCon(null);
        }
        if (prize.getNgayTao() == null) prize.setNgayTao(LocalDateTime.now());
        return adminPrize(prizeRepository.save(prize));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listSpins(Integer campaignId, String status, int page, int size) {
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        if (normalizedStatus != null && !CLAIM_STATUSES.contains(normalizedStatus)) {
            throw badRequest("Trạng thái nhận quà không hợp lệ.");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        var result = spinRepository.findForAdmin(
                campaignId,
                normalizedStatus,
                PageRequest.of(safePage, safeSize,
                        Sort.by(Sort.Direction.DESC, "ngayQuay").and(Sort.by(Sort.Direction.DESC, "id")))
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream().map(this::adminSpin).toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional
    public Map<String, Object> markDelivered(Integer spinId, Authentication authentication) {
        LuotQuayMayMan spin = spinRepository.findByIdForUpdate(spinId)
                .orElseThrow(() -> notFound("Không tìm thấy lượt quay."));
        if (!Boolean.TRUE.equals(spin.getTrungThuong())) throw badRequest("Lượt quay này không có quà để trao.");
        if (LuotQuayMayMan.DELIVERED.equals(spin.getTrangThaiNhan())) return adminSpin(spin);
        spin.setTrangThaiNhan(LuotQuayMayMan.DELIVERED);
        spin.setNgayTrao(LocalDateTime.now());
        spin.setNguoiTrao(authentication != null ? authentication.getName() : "Admin");
        return adminSpin(spinRepository.save(spin));
    }

    private VongQuayMayMan currentCampaign() {
        return campaignRepository.findActiveAt(LocalDateTime.now()).stream().findFirst().orElse(null);
    }

    private String ineligibleReason(HoaDon order, VongQuayMayMan campaign) {
        if (order.getTrangThai() == null || order.getTrangThai() != COMPLETED_ORDER) {
            return "Đơn hàng cần hoàn tất giao hàng trước khi tham gia.";
        }
        if (defaultMoney(order.getTongTien()).compareTo(campaign.getGiaTriDonToiThieu()) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu " + money(campaign.getGiaTriDonToiThieu()) + ".";
        }
        if (!Boolean.TRUE.equals(order.getDaThanhToan())) {
            return "Đơn hàng chưa được xác nhận thanh toán thành công.";
        }
        return null;
    }

    private PhanThuongVongQuay choosePrize(List<PhanThuongVongQuay> prizes) {
        long totalWeight = prizes.stream().mapToLong(prize -> Math.max(0, prize.getTrongSo())).sum();
        if (totalWeight <= 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Vòng quay chưa được cấu hình phần thưởng khả dụng.");
        long cursor = random.nextLong(totalWeight);
        for (PhanThuongVongQuay prize : prizes) {
            cursor -= prize.getTrongSo();
            if (cursor < 0) return prize;
        }
        return prizes.get(prizes.size() - 1);
    }

    private boolean canAppear(PhanThuongVongQuay prize) {
        if (prize.getTrangThai() == null || prize.getTrangThai() != 1 || prize.getTrongSo() == null || prize.getTrongSo() <= 0) return false;
        return PhanThuongVongQuay.NO_PRIZE.equals(prize.getLoaiPhanThuong())
                || (prize.getSoLuongCon() != null && prize.getSoLuongCon() > 0);
    }

    private Map<String, Object> campaignSummary(VongQuayMayMan campaign) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", campaign.getId());
        result.put("maChienDich", campaign.getMaChienDich());
        result.put("tenChienDich", campaign.getTenChienDich());
        result.put("moTa", campaign.getMoTa());
        result.put("giaTriDonToiThieu", campaign.getGiaTriDonToiThieu());
        result.put("ngayBatDau", campaign.getNgayBatDau());
        result.put("ngayKetThuc", campaign.getNgayKetThuc());
        return result;
    }

    private Map<String, Object> publicPrize(PhanThuongVongQuay prize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", prize.getId());
        result.put("tenPhanThuong", prize.getTenPhanThuong());
        result.put("loaiPhanThuong", prize.getLoaiPhanThuong());
        result.put("mauHienThi", prize.getMauHienThi());
        result.put("bieuTuong", prize.getBieuTuong());
        result.put("anhBieuTuong", prize.getAnhBieuTuong());
        result.put("thuTu", prize.getThuTu());
        return result;
    }

    private Map<String, Object> adminPrize(PhanThuongVongQuay prize) {
        Map<String, Object> result = publicPrize(prize);
        result.put("idChienDich", prize.getChienDich().getId());
        result.put("soLuongBanDau", prize.getSoLuongBanDau());
        result.put("soLuongCon", prize.getSoLuongCon());
        result.put("trongSo", prize.getTrongSo());
        result.put("trangThai", prize.getTrangThai());
        result.put("soLuotTrung", spinRepository.countByPhanThuongId(prize.getId()));
        return result;
    }

    private Map<String, Object> adminSpin(LuotQuayMayMan spin) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", spin.getId());
        result.put("idChienDich", spin.getChienDich().getId());
        result.put("tenChienDich", spin.getChienDich().getTenChienDich());
        result.put("idPhanThuong", spin.getPhanThuong().getId());
        result.put("maHoaDon", spin.getMaHoaDon());
        result.put("tenKhachHang", spin.getTenKhachHang());
        result.put("soDienThoai", spin.getSoDienThoai());
        result.put("emailKhachHang", spin.getEmailKhachHang());
        result.put("giaTriDon", spin.getGiaTriDon());
        result.put("tenKetQua", spin.getTenKetQua());
        result.put("trungThuong", spin.getTrungThuong());
        result.put("maNhanThuong", spin.getMaNhanThuong());
        result.put("trangThaiNhan", spin.getTrangThaiNhan());
        result.put("ngayQuay", spin.getNgayQuay());
        result.put("ngayTrao", spin.getNgayTrao());
        result.put("nguoiTrao", spin.getNguoiTrao());
        return result;
    }

    private Map<String, Object> eligibility(boolean eligible, String message, VongQuayMayMan campaign, HoaDon order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eligible", eligible);
        result.put("alreadyPlayed", false);
        result.put("message", message);
        if (campaign != null) result.put("campaign", campaignSummary(campaign));
        if (order != null) {
            result.put("maHoaDon", order.getMaHoaDon());
            result.put("giaTriDon", order.getTongTien());
            result.put("tenKhachHang", orderCustomerName(order));
        }
        return result;
    }

    private Map<String, Object> existingSpin(LuotQuayMayMan spin, boolean alreadyPlayed) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eligible", false);
        result.put("alreadyPlayed", alreadyPlayed);
        result.put("spinId", spin.getId());
        result.put("prizeId", spin.getPhanThuong().getId());
        result.put("bieuTuong", spin.getPhanThuong().getBieuTuong());
        result.put("anhBieuTuong", spin.getPhanThuong().getAnhBieuTuong());
        result.put("tenKetQua", spin.getTenKetQua());
        result.put("trungThuong", spin.getTrungThuong());
        result.put("maNhanThuong", Boolean.TRUE.equals(spin.getTrungThuong()) ? spin.getMaNhanThuong() : null);
        result.put("trangThaiNhan", spin.getTrangThaiNhan());
        result.put("maHoaDon", spin.getMaHoaDon());
        result.put("ngayQuay", spin.getNgayQuay());
        result.put("message", alreadyPlayed ? "Đơn hàng này đã sử dụng lượt quay." : "Đã ghi nhận kết quả vòng quay.");
        return result;
    }

    private String campaignStatus(VongQuayMayMan campaign) {
        if (campaign.getTrangThai() == null || campaign.getTrangThai() != 1) return "INACTIVE";
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(campaign.getNgayBatDau())) return "UPCOMING";
        if (now.isAfter(campaign.getNgayKetThuc())) return "ENDED";
        return "ACTIVE";
    }

    private String newClaimCode() {
        for (int i = 0; i < 10; i++) {
            String code = "ZQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!spinRepository.existsByMaNhanThuong(code)) return code;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể tạo mã nhận thưởng, vui lòng thử lại.");
    }

    private boolean phoneMatches(HoaDon order, String normalizedPhone) {
        return normalizeDigits(orderPhone(order)).equals(normalizedPhone);
    }

    private String orderPhone(HoaDon order) {
        if (order.getSoDienThoai() != null && !order.getSoDienThoai().isBlank()) return order.getSoDienThoai();
        return order.getKhachHang() != null && order.getKhachHang().getSoDienThoai() != null
                ? order.getKhachHang().getSoDienThoai() : "Không có";
    }

    private String orderEmail(HoaDon order) {
        if (order.getEmailKhachHang() != null && !order.getEmailKhachHang().isBlank()) return order.getEmailKhachHang();
        return order.getKhachHang() != null ? order.getKhachHang().getEmail() : null;
    }

    private String orderCustomerName(HoaDon order) {
        if (order.getTenKhachHang() != null && !order.getTenKhachHang().isBlank()) return order.getTenKhachHang();
        return order.getKhachHang() != null && order.getKhachHang().getHoVaTen() != null
                ? order.getKhachHang().getHoVaTen() : "Khách hàng Zestia";
    }

    private String normalizeOrderCode(String value) {
        if (value == null || value.isBlank()) throw badRequest("Vui lòng nhập mã đơn hàng.");
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 80) throw badRequest("Mã đơn hàng không hợp lệ.");
        return normalized;
    }

    private String normalizePhone(String value) {
        String normalized = normalizeDigits(value);
        if (normalized.length() < 9 || normalized.length() > 15) throw badRequest("Số điện thoại không hợp lệ.");
        return normalized;
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String money(BigDecimal value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0fđ", defaultMoney(value));
    }

    private String requiredText(Object value, String label, int min, int max) {
        String text = string(value);
        if (text == null || text.trim().length() < min) throw badRequest(label + " cần ít nhất " + min + " ký tự.");
        if (text.trim().length() > max) throw badRequest(label + " tối đa " + max + " ký tự.");
        return text.trim();
    }

    private String trimToLength(String value, int max) {
        if (value == null) return null;
        String text = value.trim();
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String string(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer integer(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try { return Integer.valueOf(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return null;
        try { return new BigDecimal(value.toString().replace(",", "")); }
        catch (NumberFormatException ignored) { return null; }
    }

    private Byte byteValue(Object value, byte fallback) {
        Integer parsed = integer(value);
        return parsed == null ? fallback : (byte) (parsed == 1 ? 1 : 0);
    }

    private LocalDateTime dateTime(Object value, String label) {
        if (value == null) throw badRequest(label + " là bắt buộc.");
        try { return LocalDateTime.parse(value.toString()); }
        catch (DateTimeParseException ignored) { throw badRequest(label + " không hợp lệ."); }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
