package com.zestia.datn.zestia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.LichSuThanhToan;
import com.zestia.datn.zestia.entity.YeuCauDoiTra;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {
    private final LichSuThanhToanRepository paymentHistoryRepo;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${payment.refund.remote-enabled:true}")
    private boolean remoteEnabled;
    @Value("${payment.momo.partner-code:MOMO}")
    private String momoPartnerCode;
    @Value("${payment.momo.access-key:}")
    private String momoAccessKey;
    @Value("${payment.momo.secret-key:}")
    private String momoSecretKey;
    @Value("${payment.momo.refund-endpoint:https://test-payment.momo.vn/v2/gateway/api/refund}")
    private String momoRefundEndpoint;
    @Value("${payment.zalopay.app-id:2553}")
    private int zaloAppId;
    @Value("${payment.zalopay.key1:}")
    private String zaloKey1;
    @Value("${payment.zalopay.refund-endpoint:https://sb-openapi.zalopay.vn/v2/refund}")
    private String zaloRefundEndpoint;
    @Value("${payment.zalopay.query-refund-endpoint:https://sb-openapi.zalopay.vn/v2/query_refund}")
    private String zaloQueryRefundEndpoint;

    public RefundOutcome refund(YeuCauDoiTra request, BigDecimal amount, String manualReference) {
        HoaDon order = request.getHoaDon();
        String method = paymentMethod(order);
        if (!ReturnExchangeService.ONLINE.equals(request.getNguon())
                || (!"MOMO".equals(method) && !"ZALOPAY".equals(method))) {
            return manualRefund(request, amount, manualReference, method);
        }
        if (!remoteEnabled) {
            return RefundOutcome.confirmed("LOCAL-" + UUID.randomUUID(), "Chế độ test: hoàn tiền được mô phỏng thành công");
        }

        LichSuThanhToan original = paymentHistoryRepo
                .findFirstByHoaDonIdAndTrangThaiOrderByNgayTaoDesc(order.getId(), "SUCCESS")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Không tìm thấy giao dịch thanh toán thành công để hoàn tiền"));
        try {
            return "MOMO".equals(method)
                    ? refundMomo(request, original, amount)
                    : refundZalo(request, original, amount);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Không thể kết nối cổng hoàn tiền. Tồn kho chưa được cập nhật: " + exception.getMessage(),
                    exception
            );
        }
    }

    public void recordSuccess(YeuCauDoiTra request, BigDecimal amount, RefundOutcome outcome) {
        if (paymentHistoryRepo.existsByMaGiaoDichAndPhuongThucAndTrangThai(
                outcome.reference(), "REFUND", "REFUND_SUCCESS")) {
            return;
        }
        paymentHistoryRepo.save(LichSuThanhToan.builder()
                .hoaDon(request.getHoaDon())
                .soTien(amount.negate())
                .phuongThuc("REFUND")
                .maGiaoDich(outcome.reference())
                .trangThai("REFUND_SUCCESS")
                .noiDung(outcome.message())
                .ngayTao(LocalDateTime.now())
                .build());
    }

    private RefundOutcome manualRefund(YeuCauDoiTra request, BigDecimal amount,
                                       String manualReference, String method) {
        String reference = clean(manualReference);
        if (reference == null || reference.length() < 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng nhập mã tham chiếu hoặc ghi chú xác nhận đã hoàn tiền (ít nhất 5 ký tự)"
            );
        }
        return RefundOutcome.confirmed(
                "MANUAL-" + request.getId() + "-" + System.currentTimeMillis(),
                "Hoàn tiền thủ công qua " + method + ": " + reference
        );
    }

    private RefundOutcome refundMomo(YeuCauDoiTra request, LichSuThanhToan original,
                                     BigDecimal amount) throws Exception {
        requireConfigured(momoPartnerCode, "MOMO_PARTNER_CODE");
        requireConfigured(momoAccessKey, "MOMO_ACCESS_KEY");
        requireConfigured(momoSecretKey, "MOMO_SECRET_KEY");
        String requestId = "ZESTIA-RF-" + request.getId();
        String orderId = requestId;
        String description = "Hoan tien " + request.getHoaDon().getMaHoaDon();
        String raw = "accessKey=" + momoAccessKey
                + "&amount=" + amount.longValue()
                + "&description=" + description
                + "&orderId=" + orderId
                + "&partnerCode=" + momoPartnerCode
                + "&requestId=" + requestId
                + "&transId=" + original.getMaGiaoDich();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", momoPartnerCode);
        payload.put("orderId", orderId);
        payload.put("requestId", requestId);
        payload.put("amount", amount.longValue());
        payload.put("transId", original.getMaGiaoDich());
        payload.put("lang", "vi");
        payload.put("description", description);
        payload.put("signature", hmacHex("HmacSHA256", momoSecretKey, raw));
        JsonNode response = postJson(momoRefundEndpoint, mapper.writeValueAsString(payload));
        int resultCode = response.path("resultCode").asInt(-1);
        String message = response.path("message").asText("MoMo không trả về nội dung");
        if (resultCode != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "MoMo từ chối hoàn tiền: " + message);
        }
        String reference = response.path("requestId").asText(requestId);
        return RefundOutcome.confirmed(reference, "MoMo xác nhận hoàn tiền: " + message);
    }

    private RefundOutcome refundZalo(YeuCauDoiTra request, LichSuThanhToan original,
                                     BigDecimal amount) throws Exception {
        requireConfigured(String.valueOf(zaloAppId), "ZALOPAY_APP_ID");
        requireConfigured(zaloKey1, "ZALOPAY_KEY1");
        if (clean(request.getMaGiaoDichHoan()) != null) {
            return queryZaloRefund(request.getMaGiaoDichHoan());
        }

        LocalDateTime createdAt = request.getNgayTao() != null ? request.getNgayTao() : LocalDateTime.now();
        String stableSuffix = UUID.nameUUIDFromBytes(
                ("zestia-refund-" + request.getId()).getBytes(StandardCharsets.UTF_8)
        ).toString().replace("-", "").substring(0, 16);
        String refundId = createdAt.format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "_" + zaloAppId + "_" + stableSuffix;
        long timestamp = System.currentTimeMillis();
        String description = "Hoan tien " + request.getHoaDon().getMaHoaDon();
        String macData = zaloAppId + "|" + original.getMaGiaoDich() + "|" + amount.longValue()
                + "|" + description + "|" + timestamp;
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", String.valueOf(zaloAppId));
        form.put("m_refund_id", refundId);
        form.put("zp_trans_id", original.getMaGiaoDich());
        form.put("amount", String.valueOf(amount.longValue()));
        form.put("timestamp", String.valueOf(timestamp));
        form.put("description", description);
        form.put("mac", hmacHex("HmacSHA256", zaloKey1, macData));
        JsonNode response = postForm(zaloRefundEndpoint, form);
        int returnCode = response.path("return_code").asInt(0);
        String message = response.path("return_message").asText("ZaloPay không trả về nội dung");
        if (returnCode != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ZaloPay từ chối yêu cầu hoàn tiền: " + message);
        }
        return RefundOutcome.pending(refundId, "ZaloPay đã tiếp nhận hoàn tiền, cần đối soát lại trạng thái");
    }

    private RefundOutcome queryZaloRefund(String refundId) throws Exception {
        long timestamp = System.currentTimeMillis();
        String macData = zaloAppId + "|" + refundId + "|" + timestamp;
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", String.valueOf(zaloAppId));
        form.put("m_refund_id", refundId);
        form.put("timestamp", String.valueOf(timestamp));
        form.put("mac", hmacHex("HmacSHA256", zaloKey1, macData));
        JsonNode response = postForm(zaloQueryRefundEndpoint, form);
        int returnCode = response.path("return_code").asInt(0);
        String message = response.path("return_message").asText("ZaloPay không trả về nội dung");
        if (returnCode == 1) {
            return RefundOutcome.confirmed(refundId, "ZaloPay xác nhận hoàn tiền thành công: " + message);
        }
        if (returnCode == 3) {
            return RefundOutcome.pending(refundId, "ZaloPay vẫn đang xử lý hoàn tiền: " + message);
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Hoàn tiền ZaloPay thất bại: " + message);
    }

    private JsonNode postJson(String endpoint, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body());
    }

    private JsonNode postForm(String endpoint, Map<String, String> values) throws Exception {
        String body = values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body());
    }

    private String paymentMethod(HoaDon order) {
        String value = clean(order.getPhuongThucThanhToanOnline());
        if (value == null) value = clean(order.getHinhThucThanhToan());
        if (value == null) return "KHAC";
        String normalized = value.toUpperCase(Locale.ROOT);
        if (normalized.contains("MOMO")) return "MOMO";
        if (normalized.contains("ZALOPAY")) return "ZALOPAY";
        return value;
    }

    private void requireConfigured(String value, String name) {
        if (clean(value) == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Thiếu cấu hình " + name);
        }
    }

    private String hmacHex(String algorithm, String key, String data) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));
        byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(result.length * 2);
        for (byte value : result) hex.append(String.format("%02x", value));
        return hex.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private String clean(String value) {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    public record RefundOutcome(boolean confirmed, boolean pending, String reference, String message) {
        static RefundOutcome confirmed(String reference, String message) {
            return new RefundOutcome(true, false, reference, message);
        }

        static RefundOutcome pending(String reference, String message) {
            return new RefundOutcome(false, true, reference, message);
        }
    }
}
