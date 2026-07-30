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
import static org.junit.jupiter.api.Assertions.assertEquals;


class TradeAnalyticsServiceTest {


    private final TradeAnalyticsService service =
            new TradeAnalyticsService();


    // =======================
    // TICKET-ADV035 VWAP TESTS
    // =======================


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



    @Test
    void vwap_serialAndParallelProduceSameResult() {

        List<EquityTrade> trades = List.of(
                equity("MSFT", "200", "5"),
                equity("MSFT", "210", "10"),
                equity("MSFT", "220", "15")
        );


        Map<String, BigDecimal> serial =
                service.vwapByInstrument(trades);



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



    // ==========================
    // TICKET-ADV034 NOTIONAL TEST
    // ==========================


    @Test
    void shouldGroupTradesByCounterparty() {

        List<EquityTrade> trades = List.of(

                equityWithCounterparty(
                        "AAPL",
                        "10",
                        "100",
                        100L
                ),

                equityWithCounterparty(
                        "MSFT",
                        "20",
                        "100",
                        100L
                ),

                equityWithCounterparty(
                        "GOOG",
                        "5",
                        "100",
                        200L
                )
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



    // ======================
    // TICKET-ADV036 P&L TEST
    // ======================


    @Test
    void pnlByInstrumentShouldCalculateProfitAndLoss() {


        List<EquityTrade> trades = List.of(

                equityWithSide(
                        "AAPL",
                        "10",
                        "100",
                        Side.BUY
                ),

                equityWithSide(
                        "AAPL",
                        "5",
                        "100",
                        Side.SELL
                ),

                equityWithSide(
                        "MSFT",
                        "20",
                        "50",
                        Side.SELL
                )
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




    // ======================
    // HELPER METHODS
    // ======================


    private EquityTrade equity(
            String symbol,
            String price,
            String quantity) {


        return equityWithCounterparty(
                symbol,
                quantity,
                price,
                1L
        );
    }



    private EquityTrade equityWithCounterparty(
            String symbol,
            String quantity,
            String price,
            long counterpartyId) {


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

                .counterpartyId(counterpartyId)

                .build();
    }



    private EquityTrade equityWithSide(
            String symbol,
            String quantity,
            String price,
            Side side) {


        return EquityTrade.builder()

                .tradeRef(
                        TradeRef.of(
                                "TRD-20260729-0002"
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

                .side(side)

                .tradeDate(
                        LocalDate.of(
                                2026,
                                7,
                                29
                        )
                )

                .counterpartyId(1L)

                .build();
    }
}