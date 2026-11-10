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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Session d'ouverture/fermeture d'une Register.
 */
@Entity
@Table(name = "cash_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashSession extends TenantEntity {

    @Column(name = "register_id", nullable = false)
    private UUID registerId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "opening_balance", precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", precision = 19, scale = 4)
    private BigDecimal closingBalance;

    @Column(name = "expected_cash", precision = 19, scale = 4)
    private BigDecimal expectedCash;

    @Column(name = "counted_cash", precision = 19, scale = 4)
    private BigDecimal countedCash;

    @Column(name = "difference", precision = 19, scale = 4)
    private BigDecimal difference;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
