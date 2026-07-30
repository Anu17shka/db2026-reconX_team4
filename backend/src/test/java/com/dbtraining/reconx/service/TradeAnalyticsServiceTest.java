package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TradeAnalyticsServiceTest {


    private final TradeAnalyticsService service =
            new TradeAnalyticsService();


    /**
     * TICKET-ADV035
     * VWAP should match hand calculated value.
     *
     * VWAP =
     * (100*10 + 110*20) / (10+20)
     *
     * = 106.666667
     */
    @Test
    void vwap_matchesHandComputedValue() {

        List<EquityTrade> trades = List.of(
                equity("AAPL", "100", "10"),
                equity("AAPL", "110", "20")
        );


        Map<String, BigDecimal> result =
                service.vwapByInstrument(trades);


        assertThat(result.get("AAPL"))
                .isEqualByComparingTo(
                        new BigDecimal("106.666667")
                );
    }



    /**
     * TICKET-ADV035
     * Serial and parallel calculations
     * should produce identical BigDecimal results.
     */
    @Test
    void vwap_serialAndParallelProduceSameResult() {


        List<EquityTrade> trades = List.of(
                equity("MSFT", "200", "5"),
                equity("MSFT", "210", "10"),
                equity("MSFT", "220", "15")
        );


        // Normal stream execution
        Map<String, BigDecimal> serial =
                service.vwapByInstrument(trades);



        // Parallel stream execution
        Map<String, BigDecimal> parallel =
                trades.parallelStream()
                        .collect(
                                Collectors.groupingBy(
                                        EquityTrade::instrumentSymbol
                                )
                        )
                        .entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> calculateVwap(
                                                entry.getValue()
                                        )
                                )
                        );


        assertThat(serial)
                .isEqualTo(parallel);
    }



    /**
     * TICKET-ADV035
     * Empty input should not throw ArithmeticException.
     */
    @Test
    void vwap_emptyInput_returnsEmptyMap() {

        Map<String, BigDecimal> result =
                service.vwapByInstrument(List.of());


        assertThat(result)
                .isEmpty();
    }



    private BigDecimal calculateVwap(
            List<EquityTrade> trades) {


        BigDecimal totalPxQty =
                BigDecimal.ZERO;

        BigDecimal totalQty =
                BigDecimal.ZERO;


        for (EquityTrade trade : trades) {

            totalPxQty =
                    totalPxQty.add(
                            trade.price()
                                    .multiply(
                                            trade.quantity()
                                    )
                    );


            totalQty =
                    totalQty.add(
                            trade.quantity()
                    );
        }


        return totalQty.signum() == 0
                ? BigDecimal.ZERO
                : totalPxQty.divide(
                        totalQty,
                        6,
                        RoundingMode.HALF_UP
                );
    }



    private EquityTrade equity(
            String symbol,
            String price,
            String quantity) {


        return EquityTrade.builder()

                .tradeRef(
                        TradeRef.of(
                                "TRD-20260729-0001"
                        )
                )

                .instrumentSymbol(symbol)

                .quantity(
                        new BigDecimal(quantity)
                )

                .price(
                        new BigDecimal(price)
                )

                .currency("USD")

                .side(Side.BUY)

                .tradeDate(
                        LocalDate.of(
                                2026,
                                7,
                                29
                        )
                )

                .counterpartyId(1L)

                .build();
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