package com.nectux.magicgold.shared;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Toute donnée métier isolée par merchantId (Merchant = Tenant, cf. 00_Master.md §2.4/§5.4).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends BaseEntity {

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
}
