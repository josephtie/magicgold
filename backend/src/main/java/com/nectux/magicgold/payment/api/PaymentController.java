package com.nectux.magicgold.payment.api;

import com.nectux.magicgold.payment.domain.Payment;
import com.nectux.magicgold.payment.service.PaymentService;
import com.nectux.magicgold.security.TenantContext;
import com.nectux.magicgold.shared.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    public record InitiateRequest(@NotNull UUID orderId, @NotNull String providerCode, @NotNull PaymentMethod method) {
    }

    public record PaymentResponse(
            UUID id,
            String paymentNumber,
            String status,
            BigDecimal amount,
            String currency,
            String externalTransactionId) {
        static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.getId(), p.getPaymentNumber(), p.getStatus().name(),
                    p.getAmount(), p.getCurrency(), p.getExternalTransactionId());
        }
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody InitiateRequest request) {
        Payment payment = paymentService.initiate(currentMerchant(), request.orderId(),
                request.providerCode(), request.method());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.get(currentMerchant(), id)));
    }

    private UUID currentMerchant() {
        UUID merchantId = TenantContext.get();
        if (merchantId == null) {
            throw new IllegalStateException("Contexte merchant absent");
        }
        return merchantId;
    }
}
