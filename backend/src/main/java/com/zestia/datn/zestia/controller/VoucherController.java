package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.GiamGia;
import com.zestia.datn.zestia.service.VoucherManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherManagementService voucherService;

    @GetMapping
    public List<GiamGia> getVouchers(Authentication authentication) {
        return voucherService.list(isAdmin(authentication));
    }

    @PostMapping
    public GiamGia addVoucher(@RequestBody GiamGia voucher) {
        return voucherService.create(voucher);
    }

    @PutMapping("/{id}")
    public GiamGia updateVoucher(@PathVariable Integer id, @RequestBody GiamGia voucher) {
        return voucherService.update(id, voucher);
    }

    @DeleteMapping("/{id}")
    public GiamGia deactivateVoucher(@PathVariable Integer id) {
        return voucherService.deactivate(id);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_Admin".equalsIgnoreCase(authority.getAuthority()));
    }
}
