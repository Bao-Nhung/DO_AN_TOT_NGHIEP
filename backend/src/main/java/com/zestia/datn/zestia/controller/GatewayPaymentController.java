package com.zestia.datn.zestia.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.entity.LichSuThanhToan;
import com.zestia.datn.zestia.entity.LichSuTracking;
import com.zestia.datn.zestia.entity.VayChiTiet;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import com.zestia.datn.zestia.repository.LichSuTrackingRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.service.EmailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tích hợp THẬT cổng thanh toán sandbox MoMo + ZaloPay.
 * Luồng: tạo đơn (đã có) -> FE gọi /momo/create hoặc /zalopay/create ->
 * BE gọi API sandbox -> trả payUrl -> FE redirect khách sang trang cổng ->
 * khách thanh toán -> cổng redirect về /momo/return | /zalopay/return ->
 * BE verify chữ ký, cập nhật đơn (đã thanh toán + xác nhận) -> redirect về FE.
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class GatewayPaymentController {
    private static final byte STATUS_CONFIRMED = 1;
    private static final byte STATUS_PAYMENT_FAILED = 7;

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final VayChiTietRepository vayChiTietRepo;
    private final LichSuThanhToanRepository lichSuRepo;
    private final LichSuTrackingRepository trackingRepo;
    private final EmailService emailService;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Payment gateway credentials are read from environment-backed properties.
    @Value("${payment.momo.partner-code:MOMO}")
    private String momoPartnerCode;
    @Value("${payment.momo.access-key:}")
    private String momoAccessKey;
    @Value("${payment.momo.secret-key:}")
    private String momoSecretKey;
    @Value("${payment.momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${payment.zalopay.app-id:2553}")
    private int zaloAppId;
    @Value("${payment.zalopay.key1:}")
    private String zaloKey1;
    @Value("${payment.zalopay.key2:}")
    private String zaloKey2;
    @Value("${payment.zalopay.endpoint:https://sb-openapi.zalopay.vn/v2/create}")
    private String zaloEndpoint;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendBaseUrl;
    @Value("${app.payment-result-url:http://localhost:5173/#/payment-result}")
    private String paymentResultUrl;

    // ============================ MoMo ============================

    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomo(@RequestBody Map<String, Object> body) {
        Integer orderId = toInt(body.get("orderId"));
        Optional<HoaDon> opt = orderId != null ? hoaDonRepo.findById(orderId) : Optional.empty();
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        HoaDon hd = opt.get();

        try {
            long amount = hd.getTongTien().longValue();
            String momoOrderId = hd.getMaHoaDon() + "T" + (System.currentTimeMillis() % 1000000);
            String orderInfo = "Thanh toan don hang " + hd.getMaHoaDon();
            String extraData = Base64.getEncoder().encodeToString(hd.getMaHoaDon().getBytes(StandardCharsets.UTF_8));

            JsonNode node = momoCreate(amount, momoOrderId, orderInfo, extraData);
            String payUrl = node.path("payUrl").asText(null);
            if (payUrl == null || payUrl.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "MoMo từ chối: " + node.path("message").asText("không rõ"),
                        "resultCode", node.path("resultCode").asInt(-1)));
            }
            hd.setHinhThucThanhToan("MOMO");
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(Map.of("payUrl", payUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi gọi MoMo: " + e.getMessage()));
        }
    }

    /** POS: tạo QR cổng MoMo theo số tiền (không gắn đơn — nhân viên xác nhận tại quầy). */
    @PostMapping("/momo/qr")
    public ResponseEntity<?> momoQr(@RequestBody Map<String, Object> body) {
        long amount = toLong(body.get("amount"));
        if (amount < 1000) return ResponseEntity.badRequest().body(Map.of("error", "Số tiền không hợp lệ"));
        try {
            String orderId = "POS" + System.currentTimeMillis();
            JsonNode node = momoCreate(amount, orderId, "Thanh toan tai quay", "");
            String qr = node.path("qrCodeUrl").asText(null);
            String payUrl = node.path("payUrl").asText(null);
            String content = (qr != null && !qr.isBlank()) ? qr : payUrl;
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "MoMo từ chối: " + node.path("message").asText("không rõ")));
            }
            return ResponseEntity.ok(Map.of("qrContent", content, "payUrl", payUrl == null ? "" : payUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi gọi MoMo: " + e.getMessage()));
        }
    }

    /** Gọi API tạo đơn MoMo, trả nguyên response. */
    private JsonNode momoCreate(long amount, String orderId, String orderInfo, String extraData) throws Exception {
        requireConfigured(momoPartnerCode, "MOMO_PARTNER_CODE");
        requireConfigured(momoAccessKey, "MOMO_ACCESS_KEY");
        requireConfigured(momoSecretKey, "MOMO_SECRET_KEY");
        String amt = String.valueOf(amount);
        String requestId = orderId;
        String redirectUrl = backendBaseUrl + "/api/payment/momo/return";
        String ipnUrl = backendBaseUrl + "/api/payment/momo/ipn";
        String requestType = "captureWallet";

        String raw = "accessKey=" + momoAccessKey +
                "&amount=" + amt +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoPartnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        String signature = hmacHex("HmacSHA256", momoSecretKey, raw);

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("partnerCode", momoPartnerCode);
        req.put("accessKey", momoAccessKey);
        req.put("requestId", requestId);
        req.put("amount", amt);
        req.put("orderId", orderId);
        req.put("orderInfo", orderInfo);
        req.put("redirectUrl", redirectUrl);
        req.put("ipnUrl", ipnUrl);
        req.put("extraData", extraData);
        req.put("requestType", requestType);
        req.put("signature", signature);
        req.put("lang", "vi");

        return postJson(momoEndpoint, mapper.writeValueAsString(req));
    }

    @GetMapping("/momo/return")
    public void momoReturn(@RequestParam Map<String, String> q, HttpServletResponse response) throws IOException {
        boolean valid = verifyMomo(q);
        String maHoaDon = decodeBase64(q.get("extraData"));
        if (maHoaDon == null || maHoaDon.isBlank()) {
            // dự phòng: bỏ hậu tố Txxx khỏi orderId
            String oid = q.getOrDefault("orderId", "");
            int t = oid.lastIndexOf('T');
            maHoaDon = t > 0 ? oid.substring(0, t) : oid;
        }
        boolean success = valid && "0".equals(q.get("resultCode"));
        finishOnline(maHoaDon, "MOMO", q.get("amount"), q.get("transId"), success);
        response.sendRedirect(paymentResultUrl + "?status=" + (success ? "success" : "failed")
                + "&orderId=" + enc(maHoaDon) + "&amount=" + enc(q.getOrDefault("amount", ""))
                + "&method=MOMO&txn=" + enc(q.getOrDefault("transId", "")));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<?> momoIpn(@RequestBody(required = false) Map<String, String> body) {
        // IPN không tới được localhost; để đầy đủ vẫn trả 200.
        return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Success"));
    }

    private boolean verifyMomo(Map<String, String> q) {
        if (isBlank(momoAccessKey) || isBlank(momoSecretKey)) return false;
        String raw = "accessKey=" + momoAccessKey +
                "&amount=" + n(q.get("amount")) +
                "&extraData=" + n(q.get("extraData")) +
                "&message=" + n(q.get("message")) +
                "&orderId=" + n(q.get("orderId")) +
                "&orderInfo=" + n(q.get("orderInfo")) +
                "&orderType=" + n(q.get("orderType")) +
                "&partnerCode=" + n(q.get("partnerCode")) +
                "&payType=" + n(q.get("payType")) +
                "&requestId=" + n(q.get("requestId")) +
                "&responseTime=" + n(q.get("responseTime")) +
                "&resultCode=" + n(q.get("resultCode")) +
                "&transId=" + n(q.get("transId"));
        try {
            return hmacHex("HmacSHA256", momoSecretKey, raw).equals(q.get("signature"));
        } catch (Exception e) { return false; }
    }

    // ============================ ZaloPay ============================

    @PostMapping("/zalopay/create")
    public ResponseEntity<?> createZalo(@RequestBody Map<String, Object> body) {
        Integer orderId = toInt(body.get("orderId"));
        Optional<HoaDon> opt = orderId != null ? hoaDonRepo.findById(orderId) : Optional.empty();
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        HoaDon hd = opt.get();

        try {
            long appTime = System.currentTimeMillis();
            String appTransId = zaloTransId(hd.getId());
            long amount = hd.getTongTien().longValue();
            JsonNode node = zaloCreate(amount, appTransId, appTime, "Thanh toan don hang " + hd.getMaHoaDon());
            String orderUrl = node.path("order_url").asText(null);
            if (orderUrl == null || orderUrl.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "ZaloPay từ chối: " + node.path("return_message").asText("không rõ")
                                + " " + node.path("sub_return_message").asText(""),
                        "return_code", node.path("return_code").asInt(-1)));
            }
            hd.setHinhThucThanhToan("ZALOPAY");
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(Map.of("payUrl", orderUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi gọi ZaloPay: " + e.getMessage()));
        }
    }

    /** POS: tạo QR cổng ZaloPay theo số tiền. */
    @PostMapping("/zalopay/qr")
    public ResponseEntity<?> zaloQr(@RequestBody Map<String, Object> body) {
        long amount = toLong(body.get("amount"));
        if (amount < 1000) return ResponseEntity.badRequest().body(Map.of("error", "Số tiền không hợp lệ"));
        try {
            long appTime = System.currentTimeMillis();
            String appTransId = zaloTransId(0); // 0 = đơn POS, không gắn hoá đơn
            JsonNode node = zaloCreate(amount, appTransId, appTime, "Thanh toan tai quay");
            String qr = node.path("qr_code").asText(null);
            String orderUrl = node.path("order_url").asText(null);
            String content = (qr != null && !qr.isBlank()) ? qr : orderUrl;
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "ZaloPay từ chối: " + node.path("return_message").asText("không rõ")));
            }
            return ResponseEntity.ok(Map.of("qrContent", content, "payUrl", orderUrl == null ? "" : orderUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi gọi ZaloPay: " + e.getMessage()));
        }
    }

    private String zaloTransId(int hoaDonId) {
        String yymmdd = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMdd"));
        return yymmdd + "_" + hoaDonId + "_" + System.currentTimeMillis(); // chứa id hoá đơn để tra lại
    }

    /** Gọi API tạo đơn ZaloPay, trả nguyên response. */
    private JsonNode zaloCreate(long amount, String appTransId, long appTime, String description) throws Exception {
        requireConfigured(String.valueOf(zaloAppId), "ZALOPAY_APP_ID");
        requireConfigured(zaloKey1, "ZALOPAY_KEY1");
        String item = "[]";
        String redirectUrl = backendBaseUrl + "/api/payment/zalopay/return";
        String embedData = mapper.writeValueAsString(Map.of("redirecturl", redirectUrl));
        String callbackUrl = backendBaseUrl + "/api/payment/zalopay/callback";
        String appUser = "zestia_user";

        // mac = HMAC256(key1, app_id|app_trans_id|app_user|amount|app_time|embed_data|item)
        String macData = zaloAppId + "|" + appTransId + "|" + appUser + "|" + amount + "|"
                + appTime + "|" + embedData + "|" + item;
        String mac = hmacHex("HmacSHA256", zaloKey1, macData);

        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", String.valueOf(zaloAppId));
        form.put("app_trans_id", appTransId);
        form.put("app_user", appUser);
        form.put("app_time", String.valueOf(appTime));
        form.put("amount", String.valueOf(amount));
        form.put("item", item);
        form.put("embed_data", embedData);
        form.put("description", description);
        form.put("bank_code", "");
        form.put("callback_url", callbackUrl);
        form.put("mac", mac);

        return postForm(zaloEndpoint, form);
    }

    @GetMapping("/zalopay/return")
    public void zaloReturn(@RequestParam Map<String, String> q, HttpServletResponse response) throws IOException {
        // apptransid dạng yyMMdd_<idHoaDon>_<time>
        String appTransId = q.getOrDefault("apptransid", "");
        Integer hoaDonId = null;
        String[] parts = appTransId.split("_");
        if (parts.length >= 2) { try { hoaDonId = Integer.parseInt(parts[1]); } catch (Exception ignored) {} }

        boolean success = "1".equals(q.get("status"));
        String maHoaDon = "";
        if (hoaDonId != null) {
            Optional<HoaDon> opt = hoaDonRepo.findById(hoaDonId);
            if (opt.isPresent()) {
                maHoaDon = opt.get().getMaHoaDon();
                applyResult(opt.get(), "ZALOPAY", new BigDecimal(n(q.getOrDefault("amount", "0"))),
                        appTransId, success);
            }
        }
        response.sendRedirect(paymentResultUrl + "?status=" + (success ? "success" : "failed")
                + "&orderId=" + enc(maHoaDon) + "&amount=" + enc(q.getOrDefault("amount", ""))
                + "&method=ZALOPAY&txn=" + enc(appTransId));
    }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<?> zaloCallback(@RequestBody(required = false) String raw) {
        // Callback server-to-server (không tới localhost); verify mac nếu nhận được.
        try {
            JsonNode node = mapper.readTree(raw);
            String data = node.path("data").asText("");
            String mac = node.path("mac").asText("");
            boolean ok = !isBlank(zaloKey2) && hmacHex("HmacSHA256", zaloKey2, data).equals(mac);
            return ResponseEntity.ok(Map.of("return_code", ok ? 1 : -1,
                    "return_message", ok ? "success" : "mac not equal"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("return_code", 0, "return_message", "error"));
        }
    }

    // ============================ Helpers ============================

    private void finishOnline(String maHoaDon, String method, String amountStr, String txn, boolean success) {
        if (maHoaDon == null || maHoaDon.isBlank()) return;
        hoaDonRepo.findByMaHoaDon(maHoaDon).ifPresent(hd -> {
            BigDecimal amt = hd.getTongTien();
            applyResult(hd, method, amt, txn, success);
        });
    }

    private void applyResult(HoaDon hd, String method, BigDecimal amount, String txn, boolean success) {
        if (success) {
            hd.setTrangThai((byte) 1);       // Đã xác nhận
            hd.setDaThanhToan(true);
            hd.setPhuongThucThanhToanOnline(method);
            hoaDonRepo.save(hd);
            emailService.sendOrderStatusUpdateEmail(hd, statusLabel(STATUS_CONFIRMED),
                    "Thanh toán " + method + " thành công. Đơn hàng đã được xác nhận.");
            lichSuRepo.save(LichSuThanhToan.builder()
                    .hoaDon(hd).soTien(amount).phuongThuc(method)
                    .maGiaoDich(txn).trangThai("SUCCESS")
                    .noiDung("Thanh toán " + method + " thành công - " + hd.getMaHoaDon())
                    .ngayTao(LocalDateTime.now()).build());
        } else {
            if (!"FAILED".equalsIgnoreCase(hd.getPhuongThucThanhToanOnline())) {
                restoreStock(hd);
            }
            hd.setDaThanhToan(false);
            hd.setPhuongThucThanhToanOnline("FAILED");
            hd.setTrangThai(STATUS_PAYMENT_FAILED);
            hd.setTrangThaiTracking("payment_failed");
            hoaDonRepo.save(hd);
            emailService.sendOrderStatusUpdateEmail(hd, statusLabel(STATUS_PAYMENT_FAILED),
                    "Thanh toán online " + method + " thất bại. Đơn hàng không được xử lý tiếp.");

            trackingRepo.save(LichSuTracking.builder()
                    .hoaDon(hd)
                    .trangThai("payment_failed")
                    .moTa("Thanh toan online " + method + " that bai. Don hang khong duoc phep xu ly tiep.")
                    .ngayCapNhat(LocalDateTime.now())
                    .build());

            lichSuRepo.save(LichSuThanhToan.builder()
                    .hoaDon(hd).soTien(BigDecimal.ZERO).phuongThuc(method)
                    .maGiaoDich(txn).trangThai("FAILED")
                    .noiDung("Thanh toán " + method + " thất bại - " + hd.getMaHoaDon())
                    .ngayTao(LocalDateTime.now()).build());
        }
    }

    private void restoreStock(HoaDon hoaDon) {
        List<HoaDonChiTiet> items = hoaDonChiTietRepo.findByHoaDonId(hoaDon.getId());
        for (HoaDonChiTiet ct : items) {
            VayChiTiet variant = ct.getVayChiTiet();
            if (variant == null || ct.getSoLuong() == null) continue;
            int currentStock = variant.getSoLuong() != null ? variant.getSoLuong() : 0;
            variant.setSoLuong(currentStock + ct.getSoLuong());
            vayChiTietRepo.save(variant);
        }
    }

    private JsonNode postJson(String url, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return mapper.readTree(resp.body());
    }

    private JsonNode postForm(String url, Map<String, String> form) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(sb.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return mapper.readTree(resp.body());
    }

    private static String hmacHex(String algo, String key, String data) throws Exception {
        Mac mac = Mac.getInstance(algo);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algo));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String decodeBase64(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8); }
        catch (Exception e) { return null; }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String n(String s) { return s == null ? "" : s; }

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number num) return num.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }

    private static String statusLabel(byte status) {
        return switch (status) {
            case 1 -> "Đã xác nhận";
            case 7 -> "Thanh toán thất bại";
            default -> "Cập nhật trạng thái";
        };
    }

    private static void requireConfigured(String value, String name) {
        if (isBlank(value)) {
            throw new IllegalStateException("Chua cau hinh bien moi truong " + name);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number num) return num.longValue();
        try { return (long) Double.parseDouble(obj.toString()); } catch (Exception e) { return 0; }
    }
}
