package com.nectux.magicgold.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Règles monétaires XOF (00_Master.md §5.5 / 18_BusinessRules.md).
 * Stockage DECIMAL(19,4), arrondi final HALF_UP sans décimale (le franc CFA n'a pas de subdivision).
 */
public final class Money {

    private Money() {
    }

    /** Arrondi d'un montant final à 0 décimale, HALF_UP. */
    public static BigDecimal roundXof(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    /** Multiplication quantité × prix unitaire, arrondie à 4 décimales (précision intermédiaire). */
    public static BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitPrice) {
        return quantity.multiply(unitPrice).setScale(4, RoundingMode.HALF_UP);
    }
}
