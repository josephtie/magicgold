package com.nectux.magicgold.reporting.api;

import com.nectux.magicgold.reporting.service.ReportingService;
import com.nectux.magicgold.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/sales")
    public ReportingService.SalesReport sales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportingService.sales(currentMerchant(), from, to);
    }

    @GetMapping("/payments")
    public ReportingService.PaymentsReport payments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportingService.payments(currentMerchant(), from, to);
    }

    @GetMapping("/reconciliation")
    public ReportingService.ReconciliationReport reconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportingService.reconciliation(currentMerchant(), from, to);
    }

    private UUID currentMerchant() {
        UUID merchantId = TenantContext.get();
        if (merchantId == null) {
            throw new IllegalStateException("Contexte merchant absent");
        }
        return merchantId;
    }
}
