package com.nectux.magicgold.order.service;

import com.nectux.magicgold.shared.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {

    @Test
    void allowedTransitions() {
        assertTrue(OrderStateMachine.canTransit(OrderStatus.DRAFT, OrderStatus.PENDING_PAYMENT));
        assertTrue(OrderStateMachine.canTransit(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID));
        assertTrue(OrderStateMachine.canTransit(OrderStatus.PAYMENT_FAILED, OrderStatus.PENDING_PAYMENT));
        assertTrue(OrderStateMachine.canTransit(OrderStatus.PAID, OrderStatus.CONFIRMED));
        assertTrue(OrderStateMachine.canTransit(OrderStatus.PAID, OrderStatus.REFUND_PENDING));
    }

    @Test
    void forbiddenTransitions() {
        assertFalse(OrderStateMachine.canTransit(OrderStatus.DRAFT, OrderStatus.PAID));
        assertFalse(OrderStateMachine.canTransit(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        assertFalse(OrderStateMachine.canTransit(OrderStatus.CANCELLED, OrderStatus.DRAFT));
        assertFalse(OrderStateMachine.canTransit(OrderStatus.REFUNDED, OrderStatus.PAID));
    }

    @Test
    void assertTransitionThrowsOnForbidden() {
        assertThrows(IllegalStateException.class,
                () -> OrderStateMachine.assertTransition(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
    }
}
