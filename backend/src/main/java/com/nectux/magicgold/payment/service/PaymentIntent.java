package com.nectux.magicgold.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Intention de paiement passée au provider (montant calculé côté serveur).
 */
public record PaymentIntent(
        UUID orderId,
        String orderNumber,
        BigDecimal amount,
        String currency,
        String reference) {
}
