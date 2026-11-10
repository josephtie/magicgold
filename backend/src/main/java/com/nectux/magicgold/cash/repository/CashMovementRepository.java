package com.nectux.magicgold.cash.repository;

import com.nectux.magicgold.cash.domain.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CashMovementRepository extends JpaRepository<CashMovement, UUID> {
    List<CashMovement> findByCashSessionId(UUID cashSessionId);
}
