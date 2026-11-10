package com.nectux.magicgold.payment.service;

/**
 * Port du domaine paiement (architecture hexagonale). Le métier ne connaît que cette interface ;
 * chaque provider (CinetPay, FeexPay, Mock...) en fournit une implémentation (00_Master.md §5.3).
 */
public interface PaymentProvider {

    /** Code unique du provider (ex. "MOCK", "CINETPAY", "FEEXPAY"). */
    String code();

    /** Déclenche un paiement chez le provider et retourne la référence externe. */
    PaymentInitiation initiate(PaymentIntent intent);

    /** Interroge le statut d'une transaction chez le provider. */
    PaymentStatusResult fetchStatus(String externalTransactionId);

    /** Vérifie la signature d'un webhook entrant. */
    boolean verifyWebhook(String payload, String signature);
}
