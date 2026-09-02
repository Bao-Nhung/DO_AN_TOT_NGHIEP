package com.zestia.datn.zestia.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.service.GatewayPaymentResultService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
import java.time.Duration;
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
@Slf4j
public class GatewayPaymentController {
    private final HoaDonRepository hoaDonRepo;
    private final JwtUtil jwtUtil;
    private final GatewayPaymentResultService paymentResultService;
    private final RequestRateLimiter rateLimiter;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
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
    @Value("${payment.momo.enabled:false}")
    private boolean momoEnabled;

    @Value("${payment.zalopay.app-id:}")
    private String zaloAppId;
    @Value("${payment.zalopay.key1:}")
    private String zaloKey1;
    @Value("${payment.zalopay.key2:}")
    private String zaloKey2;
    @Value("${payment.zalopay.endpoint:https://sb-openapi.zalopay.vn/v2/create}")
    private String zaloEndpoint;
    @Value("${payment.zalopay.query-endpoint:https://sb-openapi.zalopay.vn/v2/query}")
    private String zaloQueryEndpoint;
    @Value("${payment.zalopay.enabled:false}")
    private boolean zaloEnabled;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendBaseUrl;
    @Value("${app.payment-result-url:http://localhost:5173/payment-result}")
    private String paymentResultUrl;

    // ============================ MoMo ============================

    /** Public capability check. This never exposes gateway credentials. */
    @GetMapping("/methods")
    public Map<String, Object> paymentMethods() {
        return Map.of(
                "COD", Map.of("available", true, "sandbox", false),
                "MOMO", Map.of("available", momoAvailable(), "sandbox", isSandboxEndpoint(momoEndpoint)),
                "ZALOPAY", Map.of("available", zaloAvailable(), "sandbox", isSandboxEndpoint(zaloEndpoint))
        );
    }

    @PostMapping("/momo/create")
    @Transactional
    public ResponseEntity<?> createMomo(@RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                                        Authentication authentication,
                                        HttpServletRequest request) {
        if (!allowPaymentStart(request)) return tooManyPaymentStarts();
        if (!momoAvailable()) return gatewayUnavailable("MoMo");
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu thanh toán không hợp lệ"));
        Integer orderId = toInt(body.get("orderId"));
        Optional<HoaDon> opt = orderId != null ? hoaDonRepo.findByIdForUpdate(orderId) : Optional.empty();
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        HoaDon hd = opt.get();
        ResponseEntity<?> authError = authorizePaymentStart(hd, body, authHeader, authentication, "MOMO");
        if (authError != null) return authError;
        ResponseEntity<?> existingSession = existingPaymentSession(hd, "MOMO");
        if (existingSession != null) return existingSession;

        try {
            long amount = hd.getTongTien().longValue();
            String momoOrderId = hd.getMaHoaDon() + "T" + (System.currentTimeMillis() % 1000000);
            String orderInfo = "Thanh toan don hang " + hd.getMaHoaDon();
            String extraData = Base64.getEncoder().encodeToString(hd.getMaHoaDon().getBytes(StandardCharsets.UTF_8));

            JsonNode node = momoCreate(amount, momoOrderId, orderInfo, extraData);
            String payUrl = node.path("payUrl").asText(null);
            if (payUrl == null || payUrl.isBlank()) {
                String message = "MoMo từ chối: " + node.path("message").asText("không rõ");
                return paymentStartFailedResponse(hd, message, node.path("resultCode").asInt(-1));
            }
            hd.setHinhThucThanhToan("MOMO");
            hd.setMaGiaoDichCong(momoOrderId);
            hd.setUrlThanhToan(payUrl);
            hd.setThanhToanHetHan(LocalDateTime.now().plusMinutes(14));
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(Map.of("payUrl", payUrl));
        } catch (Exception e) {
            log.warn("MoMo payment start failed for order {}: {}", hd.getId(), e.getClass().getSimpleName());
            String message = "Không thể kết nối MoMo lúc này";
            return ResponseEntity.internalServerError().body(paymentStartFailedBody(hd, message, -1));
        }
    }

    /** POS: tạo QR cổng MoMo theo số tiền (không gắn đơn — nhân viên xác nhận tại quầy). */
    @PostMapping("/momo/qr")
    public ResponseEntity<?> momoQr(@RequestBody Map<String, Object> body) {
        if (!momoAvailable()) return gatewayUnavailable("MoMo");
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu thanh toán không hợp lệ"));
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
        GatewayPaymentResultService.PaymentOutcome outcome = null;
        if (valid) {
            outcome = paymentResultService.applyByCode(maHoaDon, "MOMO", parseAmount(q.get("amount")),
                    q.get("transId"), "0".equals(q.get("resultCode")), q.get("orderId"));
        }
        String status = !valid ? "pending" : (outcome != null && outcome.success() ? "success" : "failed");
        response.sendRedirect(paymentResultUrl + "?status=" + status
                + "&orderId=" + enc(maHoaDon) + "&amount=" + enc(q.getOrDefault("amount", ""))
                + "&method=MOMO&txn=" + enc(q.getOrDefault("transId", ""))
                + (!valid ? "&code=UNVERIFIED_RETURN" : ""));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody(required = false) Map<String, String> body) {
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean valid = verifyMomo(body);
        String maHoaDon = decodeBase64(body.get("extraData"));
        if (maHoaDon == null || maHoaDon.isBlank()) {
            String oid = body.getOrDefault("orderId", "");
            int t = oid.lastIndexOf('T');
            maHoaDon = t > 0 ? oid.substring(0, t) : oid;
        }
        if (valid) {
            paymentResultService.applyByCode(maHoaDon, "MOMO", parseAmount(body.get("amount")),
                    body.get("transId"), "0".equals(body.get("resultCode")), body.get("orderId"));
        }
        return ResponseEntity.noContent().build();
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
    @Transactional
    public ResponseEntity<?> createZalo(@RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                                        Authentication authentication,
                                        HttpServletRequest request) {
        if (!allowPaymentStart(request)) return tooManyPaymentStarts();
        if (!zaloAvailable()) return gatewayUnavailable("ZaloPay");
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu thanh toán không hợp lệ"));
        Integer orderId = toInt(body.get("orderId"));
        Optional<HoaDon> opt = orderId != null ? hoaDonRepo.findByIdForUpdate(orderId) : Optional.empty();
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        HoaDon hd = opt.get();
        ResponseEntity<?> authError = authorizePaymentStart(hd, body, authHeader, authentication, "ZALOPAY");
        if (authError != null) return authError;
        ResponseEntity<?> existingSession = existingPaymentSession(hd, "ZALOPAY");
        if (existingSession != null) return existingSession;

        try {
            long appTime = System.currentTimeMillis();
            String appTransId = zaloTransId(hd.getId());
            long amount = hd.getTongTien().longValue();
            JsonNode node = zaloCreate(amount, appTransId, appTime, "Thanh toan don hang " + hd.getMaHoaDon());
            String orderUrl = node.path("order_url").asText(null);
            if (orderUrl == null || orderUrl.isBlank()) {
                String message = "ZaloPay từ chối: " + node.path("return_message").asText("không rõ")
                        + " " + node.path("sub_return_message").asText("");
                return paymentStartFailedResponse(hd, message, node.path("return_code").asInt(-1));
            }
            hd.setHinhThucThanhToan("ZALOPAY");
            hd.setMaGiaoDichCong(appTransId);
            hd.setUrlThanhToan(orderUrl);
            hd.setThanhToanHetHan(LocalDateTime.now().plusMinutes(14));
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(Map.of("payUrl", orderUrl));
        } catch (Exception e) {
            log.warn("ZaloPay payment start failed for order {}: {}", hd.getId(), e.getClass().getSimpleName());
            String message = "Không thể kết nối ZaloPay lúc này";
            return ResponseEntity.internalServerError().body(paymentStartFailedBody(hd, message, -1));
        }
    }

    /** POS: tạo QR cổng ZaloPay theo số tiền. */
    @PostMapping("/zalopay/qr")
    public ResponseEntity<?> zaloQr(@RequestBody Map<String, Object> body) {
        if (!zaloAvailable()) return gatewayUnavailable("ZaloPay");
        if (body == null) return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu thanh toán không hợp lệ"));
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
        String appId = configuredZaloAppId();
        requireConfigured(zaloKey1, "ZALOPAY_KEY1");
        String item = "[]";
        String redirectUrl = backendBaseUrl + "/api/payment/zalopay/return";
        String embedData = mapper.writeValueAsString(Map.of("redirecturl", redirectUrl));
        String callbackUrl = backendBaseUrl + "/api/payment/zalopay/callback";
        String appUser = "zestia_user";

        // mac = HMAC256(key1, app_id|app_trans_id|app_user|amount|app_time|embed_data|item)
        String macData = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|"
                + appTime + "|" + embedData + "|" + item;
        String mac = hmacHex("HmacSHA256", zaloKey1, macData);

        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", appId);
        form.put("app_trans_id", appTransId);
        form.put("app_user", appUser);
        form.put("app_time", String.valueOf(appTime));
        form.put("amount", String.valueOf(amount));
        form.put("item", item);
        form.put("embed_data", embedData);
        form.put("description", description);
        form.put("bank_code", "");
        form.put("callback_url", callbackUrl);
        form.put("expire_duration_seconds", "900");
        form.put("mac", mac);

        return postForm(zaloEndpoint, form);
    }

    @GetMapping("/zalopay/return")
    public void zaloReturn(@RequestParam Map<String, String> q, HttpServletResponse response) throws IOException {
        String appTransId = q.getOrDefault("apptransid", "");
        Integer hoaDonId = extractHoaDonIdFromZaloTransId(appTransId);
        String maHoaDon = "";
        String status = "pending";
        String amount = q.getOrDefault("amount", "");
        String transactionId = appTransId;
        if (hoaDonId != null) {
            Optional<HoaDon> opt = hoaDonRepo.findById(hoaDonId);
            if (opt.isPresent()) {
                maHoaDon = opt.get().getMaHoaDon();
                try {
                    JsonNode queryResult = queryZaloOrder(appTransId);
                    int returnCode = queryResult.path("return_code").asInt(0);
                    boolean processing = queryResult.path("is_processing").asBoolean(false);
                    if (returnCode == 1) {
                        amount = queryResult.path("amount").asText("0");
                        transactionId = queryResult.path("zp_trans_id").asText(appTransId);
                        var outcome = paymentResultService.applyById(hoaDonId, "ZALOPAY",
                                parseAmount(amount), transactionId, true, appTransId);
                        status = outcome.success() ? "success" : "failed";
                    } else if (returnCode == 2 && !processing) {
                        var outcome = paymentResultService.applyById(hoaDonId, "ZALOPAY",
                                BigDecimal.ZERO, appTransId, false, appTransId);
                        status = outcome.success() ? "success" : "failed";
                    }
                } catch (Exception ignored) {
                    status = "pending";
                }
            }
        }
        response.sendRedirect(paymentResultUrl + "?status=" + status
                + "&orderId=" + enc(maHoaDon) + "&amount=" + enc(amount)
                + "&method=ZALOPAY&txn=" + enc(transactionId)
                + ("pending".equals(status) ? "&code=PAYMENT_PENDING" : ""));
    }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<?> zaloCallback(@RequestBody(required = false) String raw) {
        // Callback server-to-server (không tới localhost); verify mac nếu nhận được.
        try {
            JsonNode node = mapper.readTree(raw);
            String data = node.path("data").asText("");
            String mac = node.path("mac").asText("");
            boolean ok = !isBlank(zaloKey2) && hmacHex("HmacSHA256", zaloKey2, data).equals(mac);
            boolean applied = false;
            if (ok) {
                JsonNode dataNode = mapper.readTree(data);
                String appTransId = dataNode.path("app_trans_id").asText("");
                Integer hoaDonId = extractHoaDonIdFromZaloTransId(appTransId);
                if (hoaDonId != null) {
                    var outcome = paymentResultService.applyById(hoaDonId, "ZALOPAY",
                            new BigDecimal(dataNode.path("amount").asText("0")),
                            dataNode.path("zp_trans_id").asText(appTransId), true, appTransId);
                    applied = outcome.success() || outcome.idempotent();
                }
            }
            return ResponseEntity.ok(Map.of("return_code", ok && applied ? 1 : (ok ? 0 : -1),
                    "return_message", ok && applied ? "success" : (ok ? "order not applied" : "mac not equal")));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("return_code", 0, "return_message", "error"));
        }
    }

    // ============================ Helpers ============================

    private JsonNode queryZaloOrder(String appTransId) throws Exception {
        String appId = configuredZaloAppId();
        requireConfigured(zaloKey1, "ZALOPAY_KEY1");
        String macData = appId + "|" + appTransId + "|" + zaloKey1;
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", appId);
        form.put("app_trans_id", appTransId);
        form.put("mac", hmacHex("HmacSHA256", zaloKey1, macData));
        return postForm(zaloQueryEndpoint, form);
    }

    private ResponseEntity<?> authorizePaymentStart(HoaDon hd, Map<String, Object> body, String authHeader,
                                                     Authentication authentication, String expectedMethod) {
        if (Boolean.TRUE.equals(hd.getDaThanhToan())) {
            return ResponseEntity.status(409).body(Map.of("error", "Đơn hàng này đã được thanh toán"));
        }
        if (Boolean.TRUE.equals(hd.getDaHoanTonKho()) || hd.getTrangThai() == null || hd.getTrangThai() != 0) {
            return ResponseEntity.status(409).body(Map.of("error", "Đơn hàng không còn ở trạng thái chờ thanh toán"));
        }
        if (!expectedMethod.equalsIgnoreCase(cleanString(hd.getHinhThucThanhToan()))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Đơn hàng đã chọn phương thức " + hd.getHinhThucThanhToan() + ", không thể đổi cổng thanh toán"
            ));
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Phiên đăng nhập không còn hợp lệ"));
            }
            if (isStaffAuthentication(authentication)) return null;
            try {
                String token = authHeader.substring(7);
                if (!jwtUtil.isValid(token)) {
                    return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
                }
                var claims = jwtUtil.extractClaims(token);
                String role = claims.get("role", String.class);
                Integer userId = toInt(claims.get("userId"));
                if ("KhachHang".equalsIgnoreCase(role)
                        && hd.getKhachHang() != null
                        && Objects.equals(hd.getKhachHang().getId(), userId)) {
                    return null;
                }
                return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen thanh toan don hang nay"));
            } catch (Exception e) {
                return ResponseEntity.status(401).body(Map.of("error", "Token khong hop le"));
            }
        }
        if (matchesGuestPaymentProof(hd, body)) return null;
        return ResponseEntity.status(403).body(Map.of("error", "Can xac thuc don hang bang ma hoa don va so dien thoai"));
    }

    private ResponseEntity<?> existingPaymentSession(HoaDon order, String method) {
        if (order.getUrlThanhToan() == null || order.getUrlThanhToan().isBlank()
                || order.getThanhToanHetHan() == null
                || !order.getThanhToanHetHan().isAfter(LocalDateTime.now())
                || !method.equalsIgnoreCase(order.getHinhThucThanhToan())) {
            return null;
        }
        return ResponseEntity.ok(Map.of(
                "payUrl", order.getUrlThanhToan(),
                "reused", true,
                "expiresAt", order.getThanhToanHetHan()
        ));
    }

    private boolean allowPaymentStart(HttpServletRequest request) {
        String client = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        return rateLimiter.tryAcquire("payment-start", client, 15, 5 * 60);
    }

    private ResponseEntity<?> tooManyPaymentStarts() {
        return ResponseEntity.status(429).body(Map.of(
                "error", "Bạn đang yêu cầu thanh toán quá nhiều lần. Vui lòng chờ ít phút rồi thử lại"
        ));
    }

    private boolean matchesGuestPaymentProof(HoaDon hd, Map<String, Object> body) {
        String maHoaDon = cleanString(body.get("maHoaDon"));
        String phone = cleanString(body.get("soDienThoai"));
        if (maHoaDon == null || phone == null || hd.getMaHoaDon() == null) return false;
        if (!maHoaDon.equalsIgnoreCase(hd.getMaHoaDon())) return false;
        String orderPhone = hd.getSoDienThoai();
        if ((orderPhone == null || orderPhone.isBlank()) && hd.getKhachHang() != null) {
            orderPhone = hd.getKhachHang().getSoDienThoai();
        }
        return orderPhone != null && normalizePhone(phone).equals(normalizePhone(orderPhone));
    }

    private Integer extractHoaDonIdFromZaloTransId(String appTransId) {
        if (appTransId == null || appTransId.isBlank()) return null;
        String[] parts = appTransId.split("_");
        if (parts.length < 2) return null;
        try {
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(amountStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String cleanString(Object obj) {
        if (obj == null) return null;
        String value = String.valueOf(obj).trim();
        return value.isEmpty() ? null : value;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9+]", "");
        return cleaned.startsWith("+84") ? "0" + cleaned.substring(3) : cleaned;
    }

    private static boolean isStaffAuthentication(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> "ROLE_Admin".equalsIgnoreCase(authority)
                        || "ROLE_NhanVien".equalsIgnoreCase(authority)
                        || "ROLE_Nhân viên".equalsIgnoreCase(authority));
    }

    private JsonNode postJson(String url, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccessfulGatewayResponse(resp);
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
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(sb.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccessfulGatewayResponse(resp);
        return mapper.readTree(resp.body());
    }

    private static void requireSuccessfulGatewayResponse(HttpResponse<?> response) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cổng thanh toán phản hồi HTTP " + response.statusCode());
        }
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

    private ResponseEntity<Map<String, Object>> paymentStartFailedResponse(HoaDon hd, String message, int code) {
        return ResponseEntity.badRequest().body(paymentStartFailedBody(hd, message, code));
    }

    private Map<String, Object> paymentStartFailedBody(HoaDon hd, String message, int code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("paymentFailed", false);
        body.put("retryable", true);
        body.put("orderId", hd.getId());
        body.put("maHoaDon", hd.getMaHoaDon());
        body.put("amount", hd.getTongTien());
        body.put("resultCode", code);
        body.put("message", "Đơn hàng vẫn đang chờ thanh toán; bạn có thể thử lại");
        return body;
    }

    private ResponseEntity<Map<String, Object>> gatewayUnavailable(String gateway) {
        return ResponseEntity.status(503).body(Map.of(
                "error", gateway + " hiện chưa sẵn sàng. Vui lòng chọn phương thức khác hoặc thử lại sau",
                "paymentFailed", false,
                "retryable", true
        ));
    }

    private boolean momoAvailable() {
        return momoEnabled
                && !isBlank(momoPartnerCode)
                && !isBlank(momoAccessKey)
                && !isBlank(momoSecretKey)
                && isHttpsEndpoint(momoEndpoint);
    }

    private boolean zaloAvailable() {
        if (!zaloEnabled || isBlank(zaloKey1) || isBlank(zaloKey2) || !isHttpsEndpoint(zaloEndpoint)) {
            return false;
        }
        try {
            return Integer.parseInt(Objects.requireNonNull(cleanString(zaloAppId))) > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isHttpsEndpoint(String value) {
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isSandboxEndpoint(String value) {
        if (!isHttpsEndpoint(value)) return false;
        String host = URI.create(value).getHost().toLowerCase(Locale.ROOT);
        return host.contains("test") || host.startsWith("sb-") || host.contains("sandbox");
    }

    private static Integer toInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number num) return num.intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }

    private static void requireConfigured(String value, String name) {
        if (isBlank(value)) {
            throw new IllegalStateException("Chua cau hinh bien moi truong " + name);
        }
    }

    private String configuredZaloAppId() {
        String appId = cleanString(zaloAppId);
        try {
            if (appId == null || Integer.parseInt(appId) <= 0) {
                throw new NumberFormatException();
            }
            return appId;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Chua cau hinh bien moi truong ZALOPAY_APP_ID hop le");
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
