package com.nectux.magicgold.shared.enums;

/**
 * États authoritative d'une Order (00_Master.md §4.2).
 */
public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    CONFIRMED,
    PROCESSING,
    READY,
    COMPLETED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED
}
