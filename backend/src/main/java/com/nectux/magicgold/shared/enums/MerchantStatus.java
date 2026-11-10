package com.nectux.magicgold.shared.enums;

/**
 * États authoritative d'un Merchant (00_Master.md §4.3).
 */
public enum MerchantStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    BLOCKED,
    CLOSED
}
