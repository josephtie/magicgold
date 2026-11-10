package com.nectux.magicgold.payment.api;

import com.nectux.magicgold.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint public de réception des webhooks providers (idempotent, signé).
 * Exposé sans JWT (les providers appellent en server-to-server).
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    public record WebhookRequest(
            String eventType,
            String externalTransactionId,
            String externalEventId,
            String payload) {
    }

    @PostMapping("/{providerCode}")
    public ResponseEntity<Void> receive(@PathVariable String providerCode,
                                        @RequestBody WebhookRequest request,
                                        @RequestHeader(value = "X-Signature", required = false) String signature) {
        paymentService.handleWebhook(providerCode, request.eventType(), request.externalTransactionId(),
                request.externalEventId(), request.payload(), signature);
        return ResponseEntity.ok().build();
    }
}
