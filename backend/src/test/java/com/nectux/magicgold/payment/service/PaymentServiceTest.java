package com.nectux.magicgold.payment.service;

import com.nectux.magicgold.order.service.OrderService;
import com.nectux.magicgold.payment.domain.Payment;
import com.nectux.magicgold.payment.domain.WebhookEvent;
import com.nectux.magicgold.payment.repository.PaymentRepository;
import com.nectux.magicgold.payment.repository.WebhookEventRepository;
import com.nectux.magicgold.shared.ReferenceGenerator;
import com.nectux.magicgold.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    WebhookEventRepository webhookEventRepository;
    @Mock
    PaymentProviderRegistry providerRegistry;
    @Mock
    PaymentProvider provider;
    @Mock
    ReferenceGenerator referenceGenerator;
    @Mock
    OrderService orderService;

    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, webhookEventRepository,
                providerRegistry, referenceGenerator, orderService);
    }

    @Test
    void handleWebhookMarksPaymentSuccessAndOrderPaid() {
        UUID merchantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String transactionId = "MOCK-123";

        Payment payment = Payment.builder().paymentNumber("MG-PAY-1").orderId(orderId).build();
        payment.setMerchantId(merchantId);
        payment.setStatus(PaymentStatus.PENDING);

        when(providerRegistry.get("MOCK")).thenReturn(provider);
        when(provider.verifyWebhook(any(), any())).thenReturn(true);
        when(webhookEventRepository.findByExternalEventId("evt-1")).thenReturn(Optional.empty());
        when(paymentRepository.findByExternalTransactionId(transactionId)).thenReturn(Optional.of(payment));

        paymentService.handleWebhook("MOCK", "payment.success", transactionId, "evt-1", "{}", "sig");

        verify(paymentRepository).save(payment);
        verify(orderService, times(1)).markPaid(merchantId, orderId);
    }

    @Test
    void handleWebhookIsIdempotent() {
        when(providerRegistry.get("MOCK")).thenReturn(provider);
        when(provider.verifyWebhook(any(), any())).thenReturn(true);
        when(webhookEventRepository.findByExternalEventId("evt-1"))
                .thenReturn(Optional.of(new WebhookEvent()));

        paymentService.handleWebhook("MOCK", "payment.success", "MOCK-123", "evt-1", "{}", "sig");

        verify(paymentRepository, never()).findByExternalTransactionId(any());
        verify(orderService, never()).markPaid(any(), any());
    }
}
