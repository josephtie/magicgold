package com.nectux.magicgold.payment.domain;

import com.nectux.magicgold.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prestataire de paiement externe (CinetPay, FeexPay, ...). Les secrets API ne sont pas stockés ici.
 */
@Entity
@Table(name = "payment_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProvider extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", length = 32)
    private String type;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "configuration_reference")
    private String configurationReference;
}
