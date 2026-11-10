package com.nectux.magicgold.merchant.domain;

import com.nectux.magicgold.shared.BaseEntity;
import com.nectux.magicgold.shared.enums.MerchantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Merchant = Tenant. Racine de l'isolation multi-tenant (00_Master.md §2.4).
 */
@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseEntity {

    @Column(name = "merchant_number", unique = true, nullable = false, length = 32)
    private String merchantNumber;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "commercial_name", nullable = false)
    private String commercialName;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "logo_file_id")
    private UUID logoFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MerchantStatus status;
}
