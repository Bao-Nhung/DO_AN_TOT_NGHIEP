package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.StorefrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storefront")
@RequiredArgsConstructor
public class StorefrontController {
    private final StorefrontService storefrontService;

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return storefrontService.summary();
    }

    @GetMapping("/policies")
    public List<Map<String, Object>> policies() {
        return storefrontService.policies();
    }
}
