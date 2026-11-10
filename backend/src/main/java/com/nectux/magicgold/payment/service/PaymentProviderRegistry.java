package com.nectux.magicgold.payment.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registre des providers de paiement, indexé par code.
 */
@Component
public class PaymentProviderRegistry {

    private final Map<String, PaymentProvider> providers;

    public PaymentProviderRegistry(List<PaymentProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentProvider::code, Function.identity()));
    }

    public PaymentProvider get(String code) {
        PaymentProvider provider = providers.get(code);
        if (provider == null) {
            throw new IllegalArgumentException("PaymentProvider inconnu : " + code);
        }
        return provider;
    }
}
