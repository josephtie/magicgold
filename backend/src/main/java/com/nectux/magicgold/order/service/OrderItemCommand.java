package com.nectux.magicgold.order.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ligne de commande à créer (produit + quantité).
 */
public record OrderItemCommand(
        @NotNull UUID productId,
        @NotNull @Positive BigDecimal quantity) {
}
