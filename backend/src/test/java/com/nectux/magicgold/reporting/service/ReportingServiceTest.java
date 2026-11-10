package com.nectux.magicgold.reporting.service;

import com.nectux.magicgold.order.domain.Order;
import com.nectux.magicgold.order.repository.OrderRepository;
import com.nectux.magicgold.payment.domain.Payment;
import com.nectux.magicgold.payment.repository.PaymentRepository;
import com.nectux.magicgold.shared.enums.OrderStatus;
import com.nectux.magicgold.shared.enums.PaymentMethod;
import com.nectux.magicgold.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    PaymentRepository paymentRepository;

    ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(orderRepository, paymentRepository);
    }

    @Test
    void reconciliationComputesOutstanding() {
        UUID merchantId = UUID.randomUUID();
        Instant from = Instant.parse("2026-11-10T00:00:00Z");
        Instant to = Instant.parse("2026-11-11T00:00:00Z");

        Order paid = Order.builder().totalAmount(new BigDecimal("625")).status(OrderStatus.PAID).build();
        Order draft = Order.builder().totalAmount(new BigDecimal("100")).status(OrderStatus.DRAFT).build();

        Payment cash = Payment.builder().amount(new BigDecimal("180"))
                .paymentMethod(PaymentMethod.CASH).status(PaymentStatus.SUCCESS).build();
        Payment digital = Payment.builder().amount(new BigDecimal("410"))
                .paymentMethod(PaymentMethod.MOBILE_MONEY).status(PaymentStatus.SUCCESS).build();

        when(orderRepository.findByMerchantIdAndCreatedAtBetween(eq(merchantId), any(), any()))
                .thenReturn(List.of(paid, draft));
        when(paymentRepository.findByMerchantIdAndCompletedAtBetween(eq(merchantId), any(), any()))
                .thenReturn(List.of(cash, digital));

        ReportingService.ReconciliationReport report = reportingService.reconciliation(merchantId, from, to);

        assertEquals(new BigDecimal("625"), report.salesTotal());   // CA = PAID uniquement (DRAFT exclu)
        assertEquals(new BigDecimal("180"), report.cashTotal());
        assertEquals(new BigDecimal("410"), report.digitalTotal());
        assertEquals(new BigDecimal("35"), report.outstanding());    // 625 - 180 - 410
    }
}
