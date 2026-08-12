package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VoucherManagementService {
    private static final BigDecimal MAX_MONEY = new BigDecimal("9999999999999.99");
    private final GiamGiaRepository voucherRepository;

    @Transactional(readOnly = true)
    public List<GiamGia> list(boolean includeInactive) {
        return includeInactive
                ? voucherRepository.findAllByOrderByNgayTaoDescIdDesc()
                : voucherRepository.findPubliclyUsable(LocalDate.now());
    }

    @Transactional
    public GiamGia create(GiamGia request) {
        if (request == null) throw badRequest("Dữ liệu voucher không hợp lệ");
        GiamGia voucher = new GiamGia();
        voucher.setMaGiamGia(normalizeCode(request.getMaGiamGia()));
        if (voucherRepository.existsByMaGiamGiaIgnoreCase(voucher.getMaGiamGia())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã voucher đã tồn tại");
        }
        voucher.setNgayTao(LocalDateTime.now());
        applyEditableFields(voucher, request);
        return voucherRepository.save(voucher);
    }

    @Transactional
    public GiamGia update(Integer id, GiamGia request) {
        if (request == null) throw badRequest("Dữ liệu voucher không hợp lệ");
        GiamGia voucher = voucherRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"));
        applyEditableFields(voucher, request);
        return voucherRepository.save(voucher);
    }

    @Transactional
    public GiamGia deactivate(Integer id) {
        GiamGia voucher = voucherRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"));
        voucher.setTrangThai((byte) 0);
        return voucherRepository.save(voucher);
    }

    private void applyEditableFields(GiamGia target, GiamGia source) {
        String name = clean(source.getTenGiamGia());
        if (name == null || name.length() < 3 || name.length() > 200) {
            throw badRequest("Tên voucher phải từ 3 đến 200 ký tự");
        }

        BigDecimal percentage = normalizedMoney(source.getPhanTramGiam(), "Phần trăm giảm");
        BigDecimal fixed = normalizedMoney(source.getGioTriGiam(), "Giá trị giảm");
        boolean hasPercentage = positive(percentage);
        boolean hasFixed = positive(fixed);
        if (hasPercentage == hasFixed) {
            throw badRequest("Voucher phải có đúng một loại giảm: phần trăm hoặc số tiền");
        }
        if (hasPercentage && percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw badRequest("Phần trăm giảm không được vượt quá 100%");
        }

        BigDecimal minimum = normalizedMoney(source.getGiaTriDonToiThieu(), "Giá trị đơn tối thiểu");
        BigDecimal maximumDiscount = normalizedMoney(source.getGiamToiDa(), "Mức giảm tối đa");
        if (minimum == null) minimum = BigDecimal.ZERO;
        if (minimum.compareTo(BigDecimal.ZERO) < 0) throw badRequest("Giá trị đơn tối thiểu không được âm");
        if (maximumDiscount != null && maximumDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Mức giảm tối đa phải lớn hơn 0");
        }
        if (hasFixed && fixed.compareTo(minimum.max(BigDecimal.ONE)) > 0 && minimum.signum() > 0) {
            // A fixed voucher may equal the minimum order, but cannot exceed it and turn every
            // qualifying order into an unintended zero-value checkout.
            throw badRequest("Giá trị giảm cố định không được lớn hơn giá trị đơn tối thiểu");
        }

        Integer quantity = source.getSoLuong();
        if (quantity == null || quantity < 0 || quantity > 1_000_000) {
            throw badRequest("Số lượng voucher phải từ 0 đến 1.000.000");
        }
        LocalDate startsAt = source.getNgayBatDau();
        LocalDate endsAt = source.getNgayKetThuc();
        if (startsAt == null || endsAt == null) throw badRequest("Vui lòng nhập thời gian áp dụng voucher");
        if (endsAt.isBefore(startsAt)) throw badRequest("Ngày kết thúc phải từ ngày bắt đầu trở đi");
        Byte status = source.getTrangThai() == null ? (byte) 1 : source.getTrangThai();
        if (status != 0 && status != 1) throw badRequest("Trạng thái voucher không hợp lệ");

        target.setTenGiamGia(name);
        target.setGiaTriDonToiThieu(minimum);
        target.setPhanTramGiam(hasPercentage ? percentage : null);
        target.setGioTriGiam(hasFixed ? fixed : null);
        target.setGiamToiDa(hasPercentage ? maximumDiscount : null);
        target.setSoLuong(quantity);
        target.setNgayBatDau(startsAt);
        target.setNgayKetThuc(endsAt);
        target.setTrangThai(status);
    }

    private String normalizeCode(String raw) {
        String code = clean(raw);
        if (code == null) throw badRequest("Vui lòng nhập mã voucher");
        code = code.toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_-]{3,50}")) {
            throw badRequest("Mã voucher chỉ gồm chữ in hoa, số, gạch ngang hoặc gạch dưới");
        }
        return code;
    }

    private BigDecimal normalizedMoney(BigDecimal value, String label) {
        if (value == null) return null;
        if (value.scale() > 2) value = value.setScale(2, java.math.RoundingMode.HALF_UP);
        if (value.abs().compareTo(MAX_MONEY) > 0) throw badRequest(label + " vượt giới hạn cho phép");
        return value;
    }

    private boolean positive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private String clean(String value) {
        if (value == null) return null;
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
