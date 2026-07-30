package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TradeAnalyticsServiceTest {


    // ADV034 Test
    @Test
    void shouldGroupTradesByCounterparty() {

        TradeAnalyticsService service =
                new TradeAnalyticsService();


        List<EquityTrade> trades = List.of(

                EquityTrade.builder()
                        .tradeRef(new TradeRef("APP-20260729-0001"))
                        .instrumentSymbol("AAPL")
                        .quantity(new BigDecimal("10"))
                        .price(new BigDecimal("100"))
                        .currency("USD")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(100L)
                        .build(),

                EquityTrade.builder()
                        .tradeRef(new TradeRef("MSF-20260729-0002"))
                        .instrumentSymbol("MSFT")
                        .quantity(new BigDecimal("20"))
                        .price(new BigDecimal("100"))
                        .currency("USD")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(100L)
                        .build(),

                EquityTrade.builder()
                        .tradeRef(new TradeRef("GOO-20260729-0003"))
                        .instrumentSymbol("GOOG")
                        .quantity(new BigDecimal("5"))
                        .price(new BigDecimal("100"))
                        .currency("USD")
                        .side(Side.SELL)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(200L)
                        .build()
        );


        Map<Long, TradeAnalyticsService.NotionalSummary> result =
                service.notionalByCounterparty(trades);


        assertEquals(2, result.size());

        assertEquals(
                2,
                result.get(100L).count()
        );

        assertEquals(
                new BigDecimal("3000"),
                result.get(100L).total()
        );


        assertEquals(
                1,
                result.get(200L).count()
        );

        assertEquals(
                new BigDecimal("500"),
                result.get(200L).total()
        );
    }



    // ADV036 Test
    @Test
    void pnlByInstrumentShouldCalculateProfitAndLoss() {

        TradeAnalyticsService service =
                new TradeAnalyticsService();


        List<EquityTrade> trades = List.of(

                // BUY AAPL = -(10*100) = -1000
                EquityTrade.builder()
                        .tradeRef(new TradeRef("APP-20260729-0004"))
                        .instrumentSymbol("AAPL")
                        .quantity(new BigDecimal("10"))
                        .price(new BigDecimal("100"))
                        .currency("USD")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(100L)
                        .build(),


                // SELL AAPL = +(5*100) = 500
                EquityTrade.builder()
                        .tradeRef(new TradeRef("APP-20260729-0005"))
                        .instrumentSymbol("AAPL")
                        .quantity(new BigDecimal("5"))
                        .price(new BigDecimal("100"))
                        .currency("USD")
                        .side(Side.SELL)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(100L)
                        .build(),


                // SELL MSFT = +(20*50) = 1000
                EquityTrade.builder()
                        .tradeRef(new TradeRef("MSF-20260729-0006"))
                        .instrumentSymbol("MSFT")
                        .quantity(new BigDecimal("20"))
                        .price(new BigDecimal("50"))
                        .currency("USD")
                        .side(Side.SELL)
                        .tradeDate(LocalDate.of(2026, 7, 29))
                        .counterpartyId(200L)
                        .build()
        );


        Map<String, BigDecimal> result =
                service.pnlByInstrument(trades);


        assertEquals(
                new BigDecimal("-500"),
                result.get("AAPL")
        );


        assertEquals(
                new BigDecimal("1000"),
                result.get("MSFT")
        );
    }
}