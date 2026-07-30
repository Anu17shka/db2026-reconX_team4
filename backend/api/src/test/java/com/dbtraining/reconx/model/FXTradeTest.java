package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FXTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {

        FXTrade trade = sampleFx("FXX-20260603-0001");

        assertThat(trade.tradeRef())
                .isEqualTo(TradeRef.of("FXX-20260603-0001"));

        assertThat(trade.notional().currency().getCurrencyCode())
                .isEqualTo("USD");

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("110000");

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.FX);
    }


    @Test
    void builder_badIsoCode_throwsAtSetterCall() {

        FXTrade.Builder builder = FXTrade.builder();

        assertThatThrownBy(() -> builder.ccy1("EURR"))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void builder_equalCurrencies_throwsAtBuild() {

        assertThatThrownBy(() ->
                FXTrade.builder()
                        .tradeRef(TradeRef.of("FXX-20260603-0001"))
                        .ccy1("EUR")
                        .ccy2("EUR")
                        .notionalCcy1(new BigDecimal("100000"))
                        .fxRate(new BigDecimal("1.1"))
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ccy1 and ccy2 must differ");
    }


    @Test
    void builder_nonPositiveFxRate_throwsAtBuild() {

        assertThatThrownBy(() ->
                FXTrade.builder()
                        .tradeRef(TradeRef.of("FXX-20260603-0001"))
                        .ccy1("EUR")
                        .ccy2("USD")
                        .notionalCcy1(new BigDecimal("100000"))
                        .fxRate(BigDecimal.ZERO)
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("fxRate must be > 0");
    }


    @Test
    void builder_missingFxRate_throws() {

        assertThatThrownBy(() ->
                FXTrade.builder()
                        .tradeRef(TradeRef.of("FXX-20260603-0001"))
                        .ccy1("EUR")
                        .ccy2("USD")
                        .notionalCcy1(new BigDecimal("100000"))
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("fxRate");
    }


    @Test
    void equality_byTradeRef() {

        FXTrade trade1 = sampleFx("FXX-20260603-0001");

        FXTrade trade2 = FXTrade.builder()
                .tradeRef(TradeRef.of("FXX-20260603-0001"))
                .ccy1("GBP")
                .ccy2("JPY")
                .notionalCcy1(new BigDecimal("5000"))
                .fxRate(new BigDecimal("190.5"))
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 4))
                .counterpartyId(2L)
                .build();

        FXTrade trade3 = sampleFx("FXX-20260603-0002");


        assertThat(trade1)
                .isEqualTo(trade2);

        assertThat(trade1.hashCode())
                .isEqualTo(trade2.hashCode());

        assertThat(trade1)
                .isNotEqualTo(trade3);
    }


    @Test
    void toString_omitsCounterpartyId() {

        FXTrade trade = sampleFx("FXX-20260603-0001");

        assertThat(trade.toString())
                .doesNotContain("counterpartyId")
                .contains("FXX-20260603-0001")
                .contains("EUR/USD");
    }


    private FXTrade sampleFx(String ref) {

        return FXTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(new BigDecimal("1.1"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
