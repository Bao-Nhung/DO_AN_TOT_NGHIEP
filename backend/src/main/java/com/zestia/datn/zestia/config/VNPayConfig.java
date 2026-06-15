package com.zestia.datn.zestia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class VNPayConfig {

    @Value("${vnpay.tmn-code:DEMO0001}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:DEMOSECRETKEY12345678901234567890}")
    private String hashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.return-url:http://localhost:8080/api/payment/vnpay/return}")
    private String returnUrl;

    @Value("${vnpay.demo-mode:true}")
    private boolean demoMode;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public boolean isDemoMode() {
        return demoMode;
    }

    public String getTmnCode() {
        return tmnCode;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String createPaymentUrl(long amount, String orderId, String orderInfo, String ipAddr) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "fashion");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);

        LocalDateTime now = LocalDateTime.now();
        params.put("vnp_CreateDate", now.format(FMT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(FMT));

        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (val != null && !val.isEmpty()) {
                if (!query.isEmpty()) {
                    query.append('&');
                    hashData.append('&');
                }
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                     .append('=')
                     .append(URLEncoder.encode(val, StandardCharsets.US_ASCII));
                hashData.append(key).append('=').append(URLEncoder.encode(val, StandardCharsets.US_ASCII));
            }
        }

        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return payUrl + "?" + query;
    }

    public boolean validateSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        Map<String, String> sorted = new TreeMap<>(params);
        sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!hashData.isEmpty()) hashData.append('&');
                hashData.append(entry.getKey())
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            }
        }

        String computed = hmacSHA512(hashSecret, hashData.toString());
        return computed.equalsIgnoreCase(receivedHash);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }
}
