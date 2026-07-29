package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TradeToStringTest {

    @Test
    void equityTrade_toStringDoesNotExposeCounterparty() {

        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("ABC-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("250"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(999L)
                .build();

        String value = trade.toString();

        assertThat(value)
                .contains("ABC-20260603-0001")
                .contains("SAP.DE")
                .contains("100")
                .contains("250")
                .contains("EUR")
                .contains("BUY")
                .doesNotContain("999");
    }


    @Test
    void equityTrade_bigDecimalsArePlainFormat() {

        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("ABC-20260603-0002"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("1E+2"))
                .price(new BigDecimal("2E+2"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();

        String value = trade.toString();

        assertThat(value)
                .doesNotContain("E+");
    }
}