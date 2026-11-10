package com.nectux.magicgold.reporting.service;

import com.nectux.magicgold.order.domain.Order;
import com.nectux.magicgold.order.repository.OrderRepository;
import com.nectux.magicgold.payment.domain.Payment;
import com.nectux.magicgold.payment.repository.PaymentRepository;
import com.nectux.magicgold.shared.enums.OrderStatus;
import com.nectux.magicgold.shared.enums.PaymentMethod;
import com.nectux.magicgold.shared.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rapports calculés depuis PostgreSQL (agrégation en service, suffisante pour le MVP —
 * des vues d'agrégation pourront être introduites ultérieurement, cf. 03_TechnicalArchitecture §27).
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /** Statuts générateurs de CA : une Order n'est du CA qu'après paiement. */
    private static final Set<OrderStatus> REVENUE_STATUSES = EnumSet.of(
            OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
            OrderStatus.READY, OrderStatus.COMPLETED);

    public record SalesReport(long orderCount, BigDecimal revenue, Map<String, Long> ordersByStatus) {
    }

    public record PaymentsReport(BigDecimal cashTotal, BigDecimal digitalTotal, BigDecimal totalCollected,
                                 Map<String, BigDecimal> byMethod) {
    }

    public record ReconciliationReport(BigDecimal salesTotal, BigDecimal cashTotal, BigDecimal digitalTotal,
                                       BigDecimal outstanding) {
    }

    @Transactional(readOnly = true)
    public SalesReport sales(UUID merchantId, Instant from, Instant to) {
        List<Order> orders = orderRepository.findByMerchantIdAndCreatedAtBetween(merchantId, from, to);
        BigDecimal revenue = orders.stream()
                .filter(o -> REVENUE_STATUSES.contains(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));
        return new SalesReport(orders.size(), revenue, byStatus);
    }

    @Transactional(readOnly = true)
    public PaymentsReport payments(UUID merchantId, Instant from, Instant to) {
        List<Payment> success = paymentRepository.findByMerchantIdAndCompletedAtBetween(merchantId, from, to)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .toList();

        BigDecimal cash = success.stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.CASH)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal digital = success.stream()
                .filter(p -> p.getPaymentMethod() != PaymentMethod.CASH)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> byMethod = success.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentMethod().name(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
        return new PaymentsReport(cash, digital, cash.add(digital), byMethod);
    }

    @Transactional(readOnly = true)
    public ReconciliationReport reconciliation(UUID merchantId, Instant from, Instant to) {
        SalesReport sales = sales(merchantId, from, to);
        PaymentsReport payments = payments(merchantId, from, to);
        BigDecimal outstanding = sales.revenue().subtract(payments.totalCollected());
        return new ReconciliationReport(sales.revenue(), payments.cashTotal(), payments.digitalTotal(), outstanding);
    }
}
