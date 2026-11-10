package com.nectux.magicgold.payment.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Provider factice pour les tests et démonstrations. À remplacer par les adaptateurs réels
 * (CinetPay, FeexPay...) une fois les clés sandbox disponibles.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String code() {
        return "MOCK";
    }

    @Override
    public PaymentInitiation initiate(PaymentIntent intent) {
        return new PaymentInitiation("MOCK-" + UUID.randomUUID(), "PENDING", null);
    }

    @Override
    public PaymentStatusResult fetchStatus(String externalTransactionId) {
        return new PaymentStatusResult("SUCCESS", externalTransactionId);
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        return true; // le mock accepte tout (ne pas reproduire en production)
    }
}
