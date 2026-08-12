package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class VoucherApplicationService {
    private final GiamGiaRepository voucherRepository;

    public VoucherEvaluation validate(String code, BigDecimal subtotal, boolean lockForUpdate) {
        if (code == null || code.isBlank()) {
            return VoucherEvaluation.invalid("Vui lòng nhập mã giảm giá");
        }
        var voucher = lockForUpdate
                ? voucherRepository.findByMaGiamGiaIgnoreCaseForUpdate(code.trim())
                : voucherRepository.findByMaGiamGiaIgnoreCase(code.trim());
        return voucher
                .map(value -> evaluate(value, subtotal, 0))
                .orElseGet(() -> VoucherEvaluation.invalid("Mã giảm giá không tồn tại"));
    }

    public VoucherEvaluation evaluate(GiamGia voucher, BigDecimal subtotal, int reservedUnits) {
        if (voucher == null) {
            return VoucherEvaluation.invalid("Mã giảm giá không tồn tại");
        }
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (!Byte.valueOf((byte) 1).equals(voucher.getTrangThai())) {
            return VoucherEvaluation.invalid("Mã giảm giá đã ngừng hoạt động", voucher);
        }
        int remaining = voucher.getSoLuong() != null ? voucher.getSoLuong() : Integer.MAX_VALUE;
        if (remaining + Math.max(0, reservedUnits) <= 0) {
            return VoucherEvaluation.invalid("Mã giảm giá đã hết lượt sử dụng", voucher);
        }
        LocalDate today = LocalDate.now();
        if (voucher.getNgayBatDau() != null && today.isBefore(voucher.getNgayBatDau())) {
            return VoucherEvaluation.invalid("Mã giảm giá chưa đến ngày áp dụng", voucher);
        }
        if (voucher.getNgayKetThuc() != null && today.isAfter(voucher.getNgayKetThuc())) {
            return VoucherEvaluation.invalid("Mã giảm giá đã hết hạn", voucher);
        }
        if (voucher.getGiaTriDonToiThieu() != null
                && safeSubtotal.compareTo(voucher.getGiaTriDonToiThieu()) < 0) {
            return VoucherEvaluation.invalid(
                    "Đơn hàng tối thiểu " + voucher.getGiaTriDonToiThieu().longValue() + "đ để dùng mã này",
                    voucher
            );
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (positive(voucher.getPhanTramGiam())) {
            discount = safeSubtotal
                    .multiply(voucher.getPhanTramGiam())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (positive(voucher.getGiamToiDa()) && discount.compareTo(voucher.getGiamToiDa()) > 0) {
                discount = voucher.getGiamToiDa();
            }
        } else if (positive(voucher.getGioTriGiam())) {
            discount = voucher.getGioTriGiam();
        }
        if (discount.compareTo(safeSubtotal) > 0) {
            discount = safeSubtotal;
        }
        return VoucherEvaluation.valid(voucher, discount);
    }

    public VoucherEvaluation findBest(BigDecimal subtotal) {
        return findBest(subtotal, null);
    }

    public VoucherEvaluation findBest(BigDecimal subtotal, Integer reservedVoucherId) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        return voucherRepository.findPotentiallyUsable(LocalDate.now(), reservedVoucherId).stream()
                .map(voucher -> evaluate(
                        voucher,
                        safeSubtotal,
                        voucher.getId() != null && voucher.getId().equals(reservedVoucherId) ? 1 : 0
                ))
                .filter(result -> result.valid()
                        && result.discount().compareTo(BigDecimal.ZERO) > 0
                        && result.voucher().getMaGiamGia() != null
                        && !result.voucher().getMaGiamGia().isBlank())
                .max(Comparator.comparing(VoucherEvaluation::discount))
                .orElseGet(() -> VoucherEvaluation.invalid("Chưa có voucher phù hợp"));
    }

    private boolean positive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public record VoucherEvaluation(
            boolean valid,
            String message,
            GiamGia voucher,
            BigDecimal discount
    ) {
        public static VoucherEvaluation valid(GiamGia voucher, BigDecimal discount) {
            return new VoucherEvaluation(true, "Áp dụng mã thành công", voucher, discount);
        }

        public static VoucherEvaluation invalid(String message) {
            return invalid(message, null);
        }

        public static VoucherEvaluation invalid(String message, GiamGia voucher) {
            return new VoucherEvaluation(false, message, voucher, BigDecimal.ZERO);
        }
    }
}
