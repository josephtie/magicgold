package com.nectux.magicgold.stock.domain;

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
import java.util.UUID;

/**
 * Mouvement de stock traçable (append-only). Le stock courant est dérivé des mouvements.
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends TenantEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "outlet_id")
    private UUID outletId;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "reference_type", length = 32)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "note")
    private String note;
}
