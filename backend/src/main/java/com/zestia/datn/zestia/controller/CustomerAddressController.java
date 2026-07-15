package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.service.CurrentCustomerService;
import com.zestia.datn.zestia.service.CustomerAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/profile/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {
    private final CurrentCustomerService currentCustomerService;
    private final CustomerAddressService addressService;

    @GetMapping
    public List<Map<String, Object>> list(Authentication authentication) {
        return addressService.list(currentCustomerService.require(authentication));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        KhachHang customer = currentCustomerService.require(authentication);
        return addressService.create(customer, body);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id,
                                      @RequestBody Map<String, Object> body,
                                      Authentication authentication) {
        return addressService.update(currentCustomerService.require(authentication), id, body);
    }

    @PutMapping("/{id}/default")
    public Map<String, Object> setDefault(@PathVariable Integer id, Authentication authentication) {
        return addressService.setDefault(currentCustomerService.require(authentication), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Authentication authentication) {
        addressService.delete(currentCustomerService.require(authentication), id);
        return ResponseEntity.noContent().build();
    }
}
