package com.nectux.magicgold.cash.repository;

import com.nectux.magicgold.cash.domain.CashSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashSessionRepository extends JpaRepository<CashSession, UUID> {
    List<CashSession> findByMerchantId(UUID merchantId);
    Optional<CashSession> findByRegisterIdAndStatus(UUID registerId, String status);
}
