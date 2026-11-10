package com.nectux.magicgold.merchant.repository;

import com.nectux.magicgold.merchant.domain.MerchantOutlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantOutletRepository extends JpaRepository<MerchantOutlet, UUID> {
    List<MerchantOutlet> findByMerchantId(UUID merchantId);
}
