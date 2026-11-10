package com.nectux.magicgold.identity.domain;

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
 * Rôle applicatif (SUPER_ADMIN, ADMIN, MERCHANT_MANAGER, ...).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 64)
    private String name;

    @Column(name = "description")
    private String description;
}
