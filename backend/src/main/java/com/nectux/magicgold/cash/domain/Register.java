package com.nectux.magicgold.cash.domain;

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
 * Caisse physique rattachée à un MerchantOutlet.
 */
@Entity
@Table(name = "registers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Register extends TenantEntity {

    @Column(name = "outlet_id")
    private UUID outletId;

    @Column(name = "register_number", unique = true, nullable = false, length = 32)
    private String registerNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
