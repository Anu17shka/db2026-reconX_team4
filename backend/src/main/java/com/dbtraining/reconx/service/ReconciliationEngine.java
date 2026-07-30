package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 * TICKET-ADV037 — CompletableFuture: parallel recon by counterparty
 * TICKET-ADV047 — Edge cases: empty/single/all-mismatched inputs handled
 * TICKET-ADV084 — @Timed exports reconciliation_duration_seconds histogram
 *
 * WHAT:    Compares internal trades against external (counterparty) trades and
 *          returns a ReconResult per internal trade (MATCHED or BREAK).
 * HOW:     Index externals by tradeRef, then stream internals and look each
 *          up. CompletableFuture variant batches by counterparty for
 *          throughput on large books.
 * WHY:     This is the spine of the product. Everything else (REST API,
 *          Kafka consumers, dashboard) ultimately calls into here.
 * OBSERVE: Histogram appears at /actuator/prometheus under
 *          reconciliation_duration_seconds.
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
           percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        if (internal == null || internal.isEmpty()) {
            return List.of();
        }
        List<TradeType> externalTrades = external == null ? List.of() : external;
        Map<String, TradeType> externalByRef = externalTrades.stream()
                .collect(Collectors.toMap(t -> t.tradeRef().value(), Function.identity(), (a, b) -> a));
        return internal.parallelStream()
                .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
                .toList();
    }

    /**
     * TICKET-ADV037 — split by counterparty, reconcile each batch concurrently,
     * combine into a single result list. Caller passes one external feed per
     * counterparty (typical real-world shape).
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {
        // TODO(TICKET-ADV037): for each counterparty key in internalByCp launch a
        //   CompletableFuture.supplyAsync(() -> reconcile(...)). Combine via
        //   CompletableFuture.allOf(...).thenApply(v -> futures.stream()
        //       .flatMap(f -> f.join().stream()).toList()).
        throw new UnsupportedOperationException("TICKET-ADV037");
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL",
                    "No matching external trade for " + ref);
        }
        BigDecimal[] in = priceQty(internal);
        BigDecimal[] ext = priceQty(external);
        if (rule.matches(in[0], in[1], ext[0], ext[1])) {
            return ReconResult.matched(ref);
        }
        return ReconResult.breakResult(ref, "VALUE_MISMATCH",
                "internal price=%s qty=%s vs external price=%s qty=%s".formatted(in[0], in[1], ext[0], ext[1]));
    }

    /**
     * TICKET-ADV018 — exhaustive switch over the sealed hierarchy.
     * Returns {price, qty} using the rate/size pair that best represents
     * each asset class's economics: EquityTrade's literal price/quantity,
     * FXTrade's fxRate/notionalCcy1, BondTrade's couponRate/faceValue, and
     * DerivativeTrade's strike/quantity.
     */
    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case EquityTrade e     -> new BigDecimal[]{ e.price(), e.quantity() };
            case FXTrade fx        -> new BigDecimal[]{ fx.fxRate(), fx.notionalCcy1() };
            case BondTrade b       -> new BigDecimal[]{ b.couponRate(), b.faceValue() };
            case DerivativeTrade d -> new BigDecimal[]{ d.strike(), d.quantity() };
        };
    }
}
