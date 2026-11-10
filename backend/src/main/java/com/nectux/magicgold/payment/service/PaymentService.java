package com.nectux.magicgold.payment.service;

import com.nectux.magicgold.order.domain.Order;
import com.nectux.magicgold.order.service.OrderService;
import com.nectux.magicgold.payment.domain.Payment;
import com.nectux.magicgold.payment.domain.WebhookEvent;
import com.nectux.magicgold.payment.repository.PaymentRepository;
import com.nectux.magicgold.payment.repository.WebhookEventRepository;
import com.nectux.magicgold.shared.ReferenceGenerator;
import com.nectux.magicgold.shared.enums.PaymentMethod;
import com.nectux.magicgold.shared.enums.PaymentStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final PaymentProviderRegistry providerRegistry;
    private final ReferenceGenerator referenceGenerator;
    private final OrderService orderService;

    @Transactional
    public Payment initiate(UUID merchantId, UUID orderId, String providerCode, PaymentMethod method) {
        Order order = orderService.get(merchantId, orderId);
        PaymentProvider provider = providerRegistry.get(providerCode);

        Payment payment = Payment.builder()
                .paymentNumber(referenceGenerator.next("PAY"))
                .orderId(orderId)
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(method)
                .status(PaymentStatus.CREATED)
                .idempotencyKey(UUID.randomUUID().toString())
                .initiatedAt(Instant.now())
                .build();
        payment.setMerchantId(merchantId);
        payment = paymentRepository.save(payment);

        PaymentIntent intent = new PaymentIntent(orderId, order.getOrderNumber(),
                order.getTotalAmount(), order.getCurrency(), payment.getPaymentNumber());
        PaymentInitiation initiation = provider.initiate(intent);

        payment.setExternalTransactionId(initiation.externalTransactionId());
        payment.setStatus(mapStatus(initiation.status()));
        return paymentRepository.save(payment);
    }

    /**
     * Traitement idempotent d'un webhook provider (00_Master.md §5.3).
     * La confirmation de paiement provient du provider, jamais du frontend.
     */
    @Transactional
    public void handleWebhook(String providerCode, String eventType, String externalTransactionId,
                              String externalEventId, String payload, String signature) {
        PaymentProvider provider = providerRegistry.get(providerCode);
        if (!provider.verifyWebhook(payload, signature)) {
            throw new IllegalArgumentException("Signature webhook invalide");
        }

        if (webhookEventRepository.findByExternalEventId(externalEventId).isPresent()) {
            return; // événement déjà traité (idempotence)
        }

        WebhookEvent event = WebhookEvent.builder()
                .eventType(eventType)
                .externalEventId(externalEventId)
                .payload(payload)
                .status("RECEIVED")
                .receivedAt(Instant.now())
                .build();
        webhookEventRepository.save(event);

        if ("payment.success".equalsIgnoreCase(eventType)) {
            Payment payment = paymentRepository.findByExternalTransactionId(externalTransactionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Payment introuvable pour la transaction : " + externalTransactionId));
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setCompletedAt(Instant.now());
                paymentRepository.save(payment);
                if (payment.getOrderId() != null) {
                    orderService.markPaid(payment.getMerchantId(), payment.getOrderId());
                }
            }
        }

        event.setStatus("PROCESSED");
        event.setProcessedAt(Instant.now());
        webhookEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Payment get(UUID merchantId, UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .filter(p -> p.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("Payment introuvable : " + paymentId));
    }

    private PaymentStatus mapStatus(String providerStatus) {
        return switch (providerStatus == null ? "" : providerStatus.toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            case "FAILED" -> PaymentStatus.FAILED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
}
