package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradeAnalyticsService {


    // ADV034 - Notional summary by counterparty
    public Map<Long, NotionalSummary> notionalByCounterparty(
            List<? extends TradeType> trades) {

        return trades.stream()
                .collect(Collectors.groupingBy(
                        this::counterpartyIdOf,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new NotionalSummary(
                                        list.size(),
                                        list.stream()
                                                .map(t -> t.notional().amount())
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                )
                        )
                ));
    }


    // ADV036 - P&L grouped by instrument
    public Map<String, BigDecimal> pnlByInstrument(
            List<EquityTrade> equityTrades) {

        return equityTrades.stream()
                .collect(Collectors.groupingBy(
                        EquityTrade::instrumentSymbol,
                        Collectors.mapping(
                                this::pnl,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        BigDecimal::add
                                )
                        )
                ));
    }


    // ADV036 - Calculate trade P&L
    private BigDecimal pnl(EquityTrade t) {

        BigDecimal value = t.price()
                .multiply(t.quantity());

        if (t.side() == Side.SELL) {
            return value;
        }

        return value.negate();
    }


    private long counterpartyIdOf(TradeType t) {

        return switch (t) {

            case com.dbtraining.reconx.model.EquityTrade e ->
                    e.counterpartyId();

            case com.dbtraining.reconx.model.FXTrade fx ->
                    fx.counterpartyId();

            case com.dbtraining.reconx.model.BondTrade b ->
                    b.counterpartyId();

            case com.dbtraining.reconx.model.DerivativeTrade d ->
                    d.counterpartyId();
        };
    }


    public record NotionalSummary(
            long count,
            BigDecimal total
    ) {
    }
}