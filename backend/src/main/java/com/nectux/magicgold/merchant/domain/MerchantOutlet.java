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

import java.math.BigDecimal;

/**
 * Point de vente physique d'un Merchant.
 */
@Entity
@Table(name = "merchant_outlets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantOutlet extends TenantEntity {

    @Column(name = "outlet_number", unique = true, nullable = false, length = 32)
    private String outletNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
