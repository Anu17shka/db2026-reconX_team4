package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquityTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {

        EquityTrade trade = sampleEquity("SAP-20260603-0001");

        assertThat(trade.tradeRef())
                .isEqualTo(TradeRef.of("SAP-20260603-0001"));

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("10000");

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.EQUITY);
    }


    @Test
    void builder_missingPrice_throws() {

        assertThatThrownBy(() ->
                EquityTrade.builder()
                        .tradeRef(TradeRef.of("SAP-20260603-0001"))
                        .instrumentSymbol("SAP.DE")
                        .quantity(new BigDecimal("100"))
                        .currency("EUR")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("price");
    }


    @Test
    void equality_byTradeRef() {

        EquityTrade trade1 = sampleEquity("SAP-20260603-0001");

        EquityTrade trade2 = EquityTrade.builder()
                .tradeRef(TradeRef.of("SAP-20260603-0001"))
                .instrumentSymbol("MSFT")
                .quantity(new BigDecimal("200"))
                .price(new BigDecimal("50"))
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 4))
                .counterpartyId(2L)
                .build();

        EquityTrade trade3 = sampleEquity("SAP-20260603-0002");


        assertThat(trade1)
                .isEqualTo(trade2);

        assertThat(trade1.hashCode())
                .isEqualTo(trade2.hashCode());

        assertThat(trade1)
                .isNotEqualTo(trade3);
    }


    private EquityTrade sampleEquity(String ref) {

        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}