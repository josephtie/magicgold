package com.nectux.magicgold.order.service;

import com.nectux.magicgold.shared.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

/**
 * Machine à états des Order (00_Master.md §4.2).
 * Une commande annulée n'est pas réactivable.
 */
public final class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.ofEntries(
            Map.entry(OrderStatus.DRAFT, Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PENDING_PAYMENT, Set.of(
                    OrderStatus.PAID, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PAYMENT_FAILED, Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PAID, Set.of(OrderStatus.CONFIRMED, OrderStatus.REFUND_PENDING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PROCESSING, Set.of(OrderStatus.READY, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.READY, Set.of(OrderStatus.COMPLETED)),
            Map.entry(OrderStatus.REFUND_PENDING, Set.of(OrderStatus.REFUNDED)),
            Map.entry(OrderStatus.COMPLETED, Set.of()),
            Map.entry(OrderStatus.CANCELLED, Set.of()),
            Map.entry(OrderStatus.REFUNDED, Set.of()));

    private OrderStateMachine() {
    }

    public static boolean canTransit(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> targets = ALLOWED.get(from);
        return targets != null && targets.contains(to);
    }

    public static void assertTransition(OrderStatus from, OrderStatus to) {
        if (!canTransit(from, to)) {
            throw new IllegalStateException(
                    "Transition d'état interdite pour une Order : " + from + " -> " + to);
        }
    }
}
