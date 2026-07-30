package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BondTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {

        BondTrade trade = sampleBond("SAP-20260603-0001");

        assertThat(trade.tradeRef())
                .isEqualTo(TradeRef.of("SAP-20260603-0001"));

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("10000");

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.BOND);
    }


    @Test
    void builder_maturityDateBeforeTradeDate_throws() {

        assertThatThrownBy(() ->
                BondTrade.builder()
                        .tradeRef(TradeRef.of("SAP-20260603-0001"))
                        .isin("US1234567890")
                        .faceValue(new BigDecimal("10000"))
                        .couponRate(new BigDecimal("5.0"))
                        .currency("USD")
                        .maturityDate(LocalDate.of(2025, 1, 1))
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .side(Side.BUY)
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("maturityDate cannot be before tradeDate");
    }


    @Test
    void equality_byTradeRef() {

        BondTrade trade1 = sampleBond("SAP-20260603-0001");

        BondTrade trade2 = BondTrade.builder()
                .tradeRef(TradeRef.of("SAP-20260603-0001"))
                .isin("US9999999999")
                .faceValue(new BigDecimal("20000"))
                .couponRate(new BigDecimal("6.0"))
                .currency("USD")
                .maturityDate(LocalDate.of(2030, 1, 1))
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 4))
                .counterpartyId(2L)
                .build();

        BondTrade trade3 = sampleBond("SAP-20260603-0002");


        assertThat(trade1)
                .isEqualTo(trade2);

        assertThat(trade1.hashCode())
                .isEqualTo(trade2.hashCode());

        assertThat(trade1)
                .isNotEqualTo(trade3);
    }


    private BondTrade sampleBond(String ref) {

        return BondTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .isin("US1234567890")
                .faceValue(new BigDecimal("10000"))
                .couponRate(new BigDecimal("5.0"))
                .currency("USD")
                .maturityDate(LocalDate.of(2030, 1, 1))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}