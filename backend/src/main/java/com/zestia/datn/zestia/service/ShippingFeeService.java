package com.zestia.datn.zestia.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingFeeService {
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal HANOI_FEE = new BigDecimal("30000");
    private static final BigDecimal OTHER_PROVINCE_FEE = new BigDecimal("50000");

    public BigDecimal calculate(boolean directSale, BigDecimal subtotal, Object provinceCode, Object districtCode) {
        if (directSale) return BigDecimal.ZERO;
        if (subtotal != null && subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) return BigDecimal.ZERO;

        String province = normalizeCode(provinceCode);
        String district = normalizeCode(districtCode);
        if (province.isBlank() || district.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ tỉnh/thành phố và quận/huyện");
        }
        if ("1".equals(province)) {
            return "5".equals(district) ? BigDecimal.ZERO : HANOI_FEE;
        }
        return OTHER_PROVINCE_FEE;
    }

    private String normalizeCode(Object value) {
        if (value == null) return "";
        String code = String.valueOf(value).trim();
        if (!code.matches("\\d+")) return "";
        return code.replaceFirst("^0+(?!$)", "");
    }
}
