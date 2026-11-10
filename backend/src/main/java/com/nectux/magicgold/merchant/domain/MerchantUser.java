package com.nectux.magicgold.merchant.domain;

import com.nectux.magicgold.shared.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * User rattaché à un Merchant avec un rôle (Manager, Cashier, Operator).
 */
@Entity
@Table(name = "merchant_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantUser extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "outlet_id")
    private UUID outletId;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
