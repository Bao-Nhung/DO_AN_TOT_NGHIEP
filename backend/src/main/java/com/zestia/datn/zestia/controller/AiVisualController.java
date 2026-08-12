package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.AiVisualSearchService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiVisualController {

    private final AiVisualSearchService aiVisualSearchService;
    private final RequestRateLimiter rateLimiter;

    @PostMapping("/visual-search")
    public ResponseEntity<Map<String, Object>> searchByImage(@RequestParam("file") MultipartFile file,
                                                              HttpServletRequest request) {
        String client = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        if (!rateLimiter.tryAcquire("visual-search", client, 10, 10 * 60)) {
            return ResponseEntity.status(429).body(Map.of("error", "Bạn đã tìm bằng ảnh quá nhiều lần. Vui lòng thử lại sau."));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn một tập tin hình ảnh."));
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ảnh không được vượt quá 5 MB."));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tệp tải lên phải là hình ảnh."));
        }
        try {
            return ResponseEntity.ok(aiVisualSearchService.searchByImage(file));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    @GetMapping("/frequently-bought-together/{productId}")
    public ResponseEntity<Map<String, Object>> getFrequentlyBoughtTogether(@PathVariable Integer productId) {
        return ResponseEntity.ok(aiVisualSearchService.getFrequentlyBoughtTogether(productId));
    }
}
