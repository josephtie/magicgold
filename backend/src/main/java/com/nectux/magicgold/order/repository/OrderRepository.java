package com.nectux.magicgold.order.repository;

import com.nectux.magicgold.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByMerchantId(UUID merchantId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByMerchantIdAndCreatedAtBetween(UUID merchantId, Instant from, Instant to);
}
