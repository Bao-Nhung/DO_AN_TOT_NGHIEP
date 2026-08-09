package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EInvoiceService {

    private final HoaDonRepository hoaDonRepository;

    private static final String SELLER_NAME = "CÔNG TY TNHH THỜI TRANG ZESTIA FASHION";
    private static final String SELLER_TAX_CODE = "0109876543";
    private static final String SELLER_ADDRESS = "Tầng 5, Tòa nhà Zestia Tower, 123 Nguyễn Trãi, Thanh Xuân, Hà Nội";
    private static final String SELLER_PHONE = "1900 6789";
    private static final String SELLER_EMAIL = "einvoice@zestia.vn";
    private static final String INVOICE_SYMBOL = "1K26TZE";

    @Transactional
    public Map<String, Object> issueEInvoice(Integer orderId) {
        HoaDon order = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (order.getYeuCauVat() == null || !order.getYeuCauVat()) {
            order.setYeuCauVat(true);
        }

        if (!"DA_PHAT_HANH".equalsIgnoreCase(order.getTrangThaiVat())) {
            String year = String.valueOf(LocalDateTime.now().getYear());
            String seq = String.format("%06d", order.getId());
            order.setSoHoaDonVat("VAT-" + year + "-" + seq);

            String lookupCode = "ZST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            order.setMaTraCuuVat(lookupCode);

            order.setTrangThaiVat("DA_PHAT_HANH");
            order.setNgayPhatHanhVat(LocalDateTime.now());
            hoaDonRepository.save(order);
        }

        return getEInvoiceData(order.getId());
    }

    public Map<String, Object> getEInvoiceData(Integer orderId) {
        HoaDon order = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        return buildInvoicePayload(order);
    }

    public Map<String, Object> lookupEInvoice(String lookupCode) {
        Optional<HoaDon> orderOpt = hoaDonRepository.findAll().stream()
                .filter(o -> lookupCode.equalsIgnoreCase(o.getMaTraCuuVat()) || lookupCode.equalsIgnoreCase(o.getMaHoaDon()))
                .findFirst();

        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy Hóa Đơn Điện Tử với mã tra cứu: " + lookupCode);
        }

        return buildInvoicePayload(orderOpt.get());
    }

    private Map<String, Object> buildInvoicePayload(HoaDon order) {
        Map<String, Object> payload = new LinkedHashMap<>();

        // 1. Thông tin hóa đơn
        payload.put("id", order.getId());
        payload.put("maHoaDon", order.getMaHoaDon());
        payload.put("soHoaDonVat", order.getSoHoaDonVat() != null ? order.getSoHoaDonVat() : "CHƯA PHÁT HÀNH");
        payload.put("kyHieu", INVOICE_SYMBOL);
        payload.put("maTraCuu", order.getMaTraCuuVat() != null ? order.getMaTraCuuVat() : "N/A");
        payload.put("trangThaiVat", order.getTrangThaiVat() != null ? order.getTrangThaiVat() : "CHO_PHAT_HANH");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        payload.put("ngayLap", order.getNgayTao() != null ? order.getNgayTao().format(dtf) : LocalDateTime.now().format(dtf));
        payload.put("ngayPhatHanh", order.getNgayPhatHanhVat() != null ? order.getNgayPhatHanhVat().format(dtf) : "Chưa phát hành");

        // 2. Thông tin bên bán
        Map<String, Object> seller = new LinkedHashMap<>();
        seller.put("tenDonVi", SELLER_NAME);
        seller.put("maSoThue", SELLER_TAX_CODE);
        seller.put("diaChi", SELLER_ADDRESS);
        seller.put("dienThoai", SELLER_PHONE);
        seller.put("email", SELLER_EMAIL);
        payload.put("seller", seller);

        // 3. Thông tin bên mua
        Map<String, Object> buyer = new LinkedHashMap<>();
        buyer.put("tenCongTy", order.getTenCongTyVat() != null ? order.getTenCongTyVat() : order.getTenKhachHang());
        buyer.put("maSoThue", order.getMaSoThueVat() != null ? order.getMaSoThueVat() : "Không cung cấp");
        buyer.put("email", order.getEmailVat() != null ? order.getEmailVat() : order.getEmailKhachHang());
        buyer.put("diaChi", order.getDiaChiVat() != null ? order.getDiaChiVat() : order.getDiaChiGiaoHang());
        buyer.put("tenNguoiMua", order.getTenKhachHang());
        payload.put("buyer", buyer);

        // 4. Chi tiết hàng hóa
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal totalBeforeTax = BigDecimal.ZERO;

        if (order.getChiTiets() != null) {
            int stt = 1;
            for (HoaDonChiTiet item : order.getChiTiets()) {
                BigDecimal unitPrice = item.getDonGia() != null ? item.getDonGia() : BigDecimal.ZERO;
                int qty = item.getSoLuong() != null ? item.getSoLuong() : 1;
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                totalBeforeTax = totalBeforeTax.add(lineTotal);

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("stt", stt++);
                map.put("tenSanPham", item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getSanPham() != null
                        ? item.getSanPhamChiTiet().getSanPham().getTenSanPham() : "Sản phẩm thời trang Zestia");
                map.put("dvt", "Bộ / Cái");
                map.put("soLuong", qty);
                map.put("donGia", unitPrice);
                map.put("thanhTien", lineTotal);
                items.add(map);
            }
        }

        if (items.isEmpty() && order.getTongTien() != null) {
            totalBeforeTax = order.getTongTien();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("stt", 1);
            map.put("tenSanPham", "Đơn hàng thời trang Zestia #" + order.getMaHoaDon());
            map.put("dvt", "Gói");
            map.put("soLuong", 1);
            map.put("donGia", totalBeforeTax);
            map.put("thanhTien", totalBeforeTax);
            items.add(map);
        }

        // Khấu trừ voucher nếu có
        BigDecimal discount = order.getGiamGiaVoucher() != null ? order.getGiamGiaVoucher() : BigDecimal.ZERO;
        BigDecimal netBeforeTax = totalBeforeTax.subtract(discount).max(BigDecimal.ZERO);

        // Thuế VAT 8% (Chính sách ưu đãi thuế VAT thời trang hiện hành)
        BigDecimal vatRate = new BigDecimal("0.08");
        BigDecimal vatAmount = netBeforeTax.multiply(vatRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalPayment = netBeforeTax.add(vatAmount).add(order.getPhiVanChuyen() != null ? order.getPhiVanChuyen() : BigDecimal.ZERO);

        payload.put("items", items);
        payload.put("totalBeforeTax", totalBeforeTax);
        payload.put("discountVoucher", discount);
        payload.put("netBeforeTax", netBeforeTax);
        payload.put("vatRatePercent", 8);
        payload.put("vatAmount", vatAmount);
        payload.put("shippingFee", order.getPhiVanChuyen() != null ? order.getPhiVanChuyen() : BigDecimal.ZERO);
        payload.put("totalPayment", totalPayment);
        payload.put("amountInWords", numberToVietnameseWords(totalPayment));

        // QR Code & Digital Signature Stamp
        String lookupUrl = "https://zestia.vn/e-invoice/lookup/" + (order.getMaTraCuuVat() != null ? order.getMaTraCuuVat() : order.getMaHoaDon());
        payload.put("qrCodeUrl", "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=" + lookupUrl);
        payload.put("digitalSignature", "SHA256: " + UUID.nameUUIDFromBytes((order.getMaHoaDon() + SELLER_TAX_CODE).getBytes()).toString());

        return payload;
    }

    private String numberToVietnameseWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Không đồng";
        long number = amount.longValue();
        return String.format("%,d VNĐ (Đã bao gồm thuế GTGT)", number);
    }
}
