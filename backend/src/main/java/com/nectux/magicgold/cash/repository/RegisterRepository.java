package com.nectux.magicgold.cash.repository;

import com.nectux.magicgold.cash.domain.Register;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegisterRepository extends JpaRepository<Register, UUID> {
    List<Register> findByMerchantId(UUID merchantId);
}
