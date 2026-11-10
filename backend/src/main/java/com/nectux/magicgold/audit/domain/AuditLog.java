package com.nectux.magicgold.audit.domain;

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
 * Journal d'audit des opérations sensibles. Non modifiable par les utilisateurs ordinaires.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "entity", nullable = false, length = 64)
    private String entity;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "device", length = 128)
    private String device;
}
