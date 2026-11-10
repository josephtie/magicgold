package com.nectux.magicgold.stock.repository;

import com.nectux.magicgold.stock.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findByMerchantIdAndProductId(UUID merchantId, UUID productId);
}
