package com.nectux.magicgold.payment.service;

/**
 * Statut d'une transaction retourné par le provider.
 */
public record PaymentStatusResult(
        String status,
        String externalTransactionId) {
}
