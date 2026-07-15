package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public Map<String, Object> storeReviews(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "9") int size,
                                            @RequestParam(required = false) Integer stars) {
        return reviewService.storeReviews(page, size, stars);
    }

    @GetMapping("/product/{productId}")
    public Map<String, Object> reviews(@PathVariable Integer productId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "6") int size) {
        return reviewService.reviews(productId, page, size);
    }

    @GetMapping("/product/{productId}/eligibility")
    public Map<String, Object> eligibility(@PathVariable Integer productId, Authentication authentication) {
        return reviewService.eligibility(productId, authentication);
    }

    @PostMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> create(@PathVariable Integer productId,
                                      @RequestParam Integer orderId,
                                      @RequestParam int stars,
                                      @RequestParam String content,
                                      @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                      Authentication authentication) {
        return reviewService.create(productId, orderId, stars, content, images, authentication);
    }
}
