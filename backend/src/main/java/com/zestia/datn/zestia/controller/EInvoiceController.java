package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.service.EInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EInvoiceController {

    private final EInvoiceService eInvoiceService;

    @PostMapping("/orders/{orderId}/issue-e-invoice")
    public ResponseEntity<Map<String, Object>> issueEInvoice(@PathVariable Integer orderId) {
        return ResponseEntity.ok(eInvoiceService.issueEInvoice(orderId));
    }

    @GetMapping("/orders/{orderId}/e-invoice")
    public ResponseEntity<Map<String, Object>> getEInvoice(@PathVariable Integer orderId) {
        return ResponseEntity.ok(eInvoiceService.getEInvoiceData(orderId));
    }

    @GetMapping("/e-invoice/lookup/{lookupCode}")
    public ResponseEntity<Map<String, Object>> lookupEInvoice(@PathVariable String lookupCode) {
        return ResponseEntity.ok(eInvoiceService.lookupEInvoice(lookupCode));
    }
}
