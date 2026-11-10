package com.nectux.magicgold.merchant.repository;

import com.nectux.magicgold.merchant.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByMerchantNumber(String merchantNumber);
}
