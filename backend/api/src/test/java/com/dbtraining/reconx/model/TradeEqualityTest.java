package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TradeEqualityTest {

    private EquityTrade equityTrade(String ref, String qty, String price) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal(qty))
                .price(new BigDecimal(price))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }


    @Test
    void sameTradeRef_objectsAreEqual() {

        EquityTrade t1 =
                equityTrade("TRD-20260603-0001", "100", "100");

        EquityTrade t2 =
                equityTrade("TRD-20260603-0001", "500", "250");

        assertThat(t1)
                .isEqualTo(t2);

        assertThat(new HashSet<>(List.of(t1, t2)))
                .hasSize(1);
    }


    @Test
    void equalObjects_haveSameHashCode() {

        EquityTrade t1 =
                equityTrade("TRD-20260603-0002", "100", "100");

        EquityTrade t2 =
                equityTrade("TRD-20260603-0002", "900", "300");

        assertThat(t1.hashCode())
                .isEqualTo(t2.hashCode());
    }


    @Test
    void differentTradeRef_notEqual() {

        EquityTrade t1 =
                equityTrade("TRD-20260603-0003", "100", "100");

        EquityTrade t2 =
                equityTrade("TRD-20260603-0004", "100", "100");

        assertThat(t1)
                .isNotEqualTo(t2);
    }
}