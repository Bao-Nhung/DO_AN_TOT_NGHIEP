package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.AiVisualSearchService;
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

    @PostMapping("/visual-search")
    public ResponseEntity<Map<String, Object>> searchByImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn một tập tin hình ảnh."));
        }
        return ResponseEntity.ok(aiVisualSearchService.searchByImage(file));
    }

    @GetMapping("/frequently-bought-together/{productId}")
    public ResponseEntity<Map<String, Object>> getFrequentlyBoughtTogether(@PathVariable Integer productId) {
        return ResponseEntity.ok(aiVisualSearchService.getFrequentlyBoughtTogether(productId));
    }
}
