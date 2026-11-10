package com.nectux.magicgold.order.service;

import com.nectux.magicgold.catalog.domain.Product;
import com.nectux.magicgold.catalog.repository.ProductRepository;
import com.nectux.magicgold.order.domain.Order;
import com.nectux.magicgold.order.domain.OrderItem;
import com.nectux.magicgold.order.domain.OrderStatusHistory;
import com.nectux.magicgold.order.repository.OrderItemRepository;
import com.nectux.magicgold.order.repository.OrderRepository;
import com.nectux.magicgold.order.repository.OrderStatusHistoryRepository;
import com.nectux.magicgold.shared.Money;
import com.nectux.magicgold.shared.ReferenceGenerator;
import com.nectux.magicgold.shared.enums.OrderStatus;
import com.nectux.magicgold.stock.domain.StockMovement;
import com.nectux.magicgold.stock.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ReferenceGenerator referenceGenerator;

    @Transactional
    public Order createOrder(UUID merchantId, UUID customerId, UUID outletId,
                             BigDecimal discountAmount, List<OrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins une ligne");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCommand item : items) {
            Product product = productRepository.findById(item.productId())
                    .filter(p -> p.getMerchantId().equals(merchantId))
                    .orElseThrow(() -> new IllegalArgumentException("Produit introuvable : " + item.productId()));

            BigDecimal lineTotal = Money.lineTotal(item.quantity(), product.getUnitPrice());
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productNameSnapshot(product.getName())
                    .quantity(item.quantity())
                    .unitPrice(product.getUnitPrice())
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(lineTotal)
                    .build();
            orderItem.setMerchantId(merchantId);
            orderItems.add(orderItem);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal total = Money.roundXof(subtotal.subtract(discount));

        Order order = Order.builder()
                .orderNumber(referenceGenerator.next("ORD"))
                .customerId(customerId)
                .outletId(outletId)
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(total)
                .currency("XOF")
                .status(OrderStatus.DRAFT)
                .build();
        order.setMerchantId(merchantId);

        Order saved = orderRepository.save(order);
        for (OrderItem oi : orderItems) {
            oi.setOrderId(saved.getId());
            orderItemRepository.save(oi);
        }
        recordTransition(saved, null, OrderStatus.DRAFT);
        return saved;
    }

    @Transactional(readOnly = true)
    public Order get(UUID merchantId, UUID orderId) {
        return orderRepository.findById(orderId)
                .filter(o -> o.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("Order introuvable : " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Order> list(UUID merchantId) {
        return orderRepository.findByMerchantId(merchantId);
    }

    @Transactional
    public Order transition(UUID merchantId, UUID orderId, OrderStatus target) {
        Order order = get(merchantId, orderId);
        OrderStatus from = order.getStatus();
        OrderStateMachine.assertTransition(from, target);
        order.setStatus(target);
        Order saved = orderRepository.save(order);
        recordTransition(saved, from, target);
        return saved;
    }

    /**
     * Paiement confirmé : passage à PAID + décrément du stock (mouvements traçables).
     */
    @Transactional
    public Order markPaid(UUID merchantId, UUID orderId) {
        Order order = transition(merchantId, orderId, OrderStatus.PAID);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem oi : items) {
            StockMovement movement = StockMovement.builder()
                    .productId(oi.getProductId())
                    .outletId(order.getOutletId())
                    .type("SALE")
                    .quantity(oi.getQuantity().negate())
                    .referenceType("ORDER")
                    .referenceId(order.getId())
                    .note("Vente " + order.getOrderNumber())
                    .build();
            movement.setMerchantId(merchantId);
            stockMovementRepository.save(movement);
        }
        return order;
    }

    private void recordTransition(Order order, OrderStatus from, OrderStatus to) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .fromStatus(from)
                .toStatus(to)
                .changedAt(Instant.now())
                .build();
        history.setMerchantId(order.getMerchantId());
        statusHistoryRepository.save(history);
    }
}
