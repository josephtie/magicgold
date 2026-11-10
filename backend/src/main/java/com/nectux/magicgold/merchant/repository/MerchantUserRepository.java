package com.nectux.magicgold.merchant.repository;

import com.nectux.magicgold.merchant.domain.MerchantUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantUserRepository extends JpaRepository<MerchantUser, UUID> {
    List<MerchantUser> findByMerchantId(UUID merchantId);
    List<MerchantUser> findByUserId(UUID userId);
}
