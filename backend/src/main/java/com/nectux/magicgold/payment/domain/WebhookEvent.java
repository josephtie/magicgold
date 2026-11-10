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

import java.time.Instant;
import java.util.UUID;

/**
 * Événement webhook reçu d'un système externe, stocké pour idempotence (00_Master.md §5.3).
 */
@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent extends BaseEntity {

    @Column(name = "provider_id")
    private UUID providerId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "external_event_id", nullable = false, unique = true)
    private String externalEventId;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
