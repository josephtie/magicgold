package com.nectux.magicgold.shared.enums;

/**
 * États authoritative d'un Payment (00_Master.md §4.1).
 */
public enum PaymentStatus {
    CREATED,
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    PARTIALLY_REFUNDED,
    REFUNDED
}
