package com.nectux.magicgold.payment.repository;

import com.nectux.magicgold.payment.domain.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, UUID> {
    Optional<PaymentProvider> findByCode(String code);
}
