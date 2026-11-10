package com.nectux.magicgold.payment.service;

/**
 * Résultat de l'initiation d'un paiement chez le provider.
 */
public record PaymentInitiation(
        String externalTransactionId,
        String status,
        String redirectUrl) {
}
