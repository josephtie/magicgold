package com.nectux.magicgold.payment.repository;

import com.nectux.magicgold.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByMerchantId(UUID merchantId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByExternalTransactionId(String externalTransactionId);
}
