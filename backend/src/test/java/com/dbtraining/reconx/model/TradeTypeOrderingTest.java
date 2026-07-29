package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class TradeTypeOrderingTest {


    @Test
    void treeSet_ordersNewestTradeFirst() {

        TradeType oldTrade = createEquity(
                "EQU-20260101-0001",
                LocalDate.of(2026, 1, 1)
        );

        TradeType newTrade = createEquity(
                "EQU-20260601-0001",
                LocalDate.of(2026, 6, 1)
        );


        TreeSet<TradeType> trades = new TreeSet<>();

        trades.add(oldTrade);
        trades.add(newTrade);


        assertThat(trades.first())
                .isEqualTo(newTrade);

        assertThat(trades.last())
                .isEqualTo(oldTrade);
    }


    @Test
    void sameTradeDateUsesTradeRefAsTieBreaker() {

        TradeType trade1 = createEquity(
                "EQU-20260601-0001",
                LocalDate.of(2026, 6, 1)
        );

        TradeType trade2 = createEquity(
                "EQU-20260601-0002",
                LocalDate.of(2026, 6, 1)
        );


        TreeSet<TradeType> trades = new TreeSet<>();

        trades.add(trade2);
        trades.add(trade1);


        assertThat(trades.first())
                .isEqualTo(trade1);

        assertThat(trades.last())
                .isEqualTo(trade2);
    }


    @Test
    void compareToReturnsZeroForSameDateAndSameTradeRef() {

        TradeType trade1 = createEquity(
                "EQU-20260601-0001",
                LocalDate.of(2026, 6, 1)
        );

        TradeType trade2 = createEquity(
                "EQU-20260601-0001",
                LocalDate.of(2026, 6, 1)
        );


        assertThat(trade1.compareTo(trade2))
                .isEqualTo(0);
    }


    @Test
    void treeSetAcceptsTradeType() {

        TreeSet<TradeType> trades = new TreeSet<>();

        trades.add(createEquity(
                "EQU-20260601-0001",
                LocalDate.of(2026, 6, 1)
        ));

        trades.add(createEquity(
                "EQU-20260602-0001",
                LocalDate.of(2026, 6, 2)
        ));


        assertThat(trades)
                .hasSize(2);
    }


    private TradeType createEquity(String ref, LocalDate date) {

        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(date)
                .counterpartyId(1L)
                .build();
    }
}