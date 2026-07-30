package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerivativeTradeTest {

    @Test
    void callOption_buildsSuccessfully() {

        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("OPT-20260603-0001"))
                .underlying("AAPL")
                .strike(new BigDecimal("200"))
                .quantity(new BigDecimal("10"))
                .expiry(LocalDate.of(2026, 12, 1))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();

        assertThat(trade.optionType())
                .isEqualTo(DerivativeTrade.OptionType.CALL);

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }


    @Test
    void historicalExpiry_isAllowed() {

        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("OPT-20260603-0002"))
                .underlying("AAPL")
                .strike(new BigDecimal("200"))
                .quantity(new BigDecimal("10"))
                .expiry(LocalDate.of(2025, 6, 1))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2025, 1, 1))
                .counterpartyId(1L)
                .build();

        assertThat(trade.expiry())
                .isEqualTo(LocalDate.of(2025, 6, 1));
    }


    @Test
    void expiryBeforeTradeDate_throws() {

        assertThatThrownBy(() ->
                DerivativeTrade.builder()
                        .tradeRef(TradeRef.of("OPT-20260603-0003"))
                        .underlying("AAPL")
                        .strike(new BigDecimal("200"))
                        .quantity(new BigDecimal("10"))
                        .expiry(LocalDate.of(2025, 1, 1))
                        .optionType(DerivativeTrade.OptionType.CALL)
                        .currency("USD")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 1, 1))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("expiry cannot be before tradeDate");
    }
}