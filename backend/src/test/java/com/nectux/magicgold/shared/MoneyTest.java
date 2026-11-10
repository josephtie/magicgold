package com.nectux.magicgold.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void roundXofHalfUp() {
        assertEquals(new BigDecimal("626"), Money.roundXof(new BigDecimal("625.5")));
        assertEquals(new BigDecimal("625"), Money.roundXof(new BigDecimal("625.4")));
        assertEquals(new BigDecimal("626"), Money.roundXof(new BigDecimal("625.5000")));
    }

    @Test
    void lineTotalKeepsFourDecimals() {
        assertEquals(new BigDecimal("30.0000"), Money.lineTotal(new BigDecimal("3"), new BigDecimal("10")));
        assertEquals(new BigDecimal("33.3333"), Money.lineTotal(new BigDecimal("1"), new BigDecimal("33.3333")));
    }
}
