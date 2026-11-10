package com.nectux.magicgold.config.domain;

import com.nectux.magicgold.shared.BaseEntity;
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
 * Configuration système (globale si merchant_id est null, sinon spécifique au Merchant).
 */
@Entity
@Table(name = "configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration extends BaseEntity {

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(name = "config_value", columnDefinition = "text")
    private String value;

    @Column(name = "value_type", length = 32)
    private String valueType;

    @Column(name = "description")
    private String description;
}
