package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * ============================================================================
 * TICKET-ADV039 — Optional chaining for null-safe lookups
 *
 * WHAT:    Walks from a trade reference to its counterparty using Optional
 *          combinators only — no if (x != null), no isPresent()/get().
 * HOW:     tradeRepo.findByTradeRef(ref) already returns Optional<Trade>;
 *          Trade.getCounterparty() is an eager-enough @ManyToOne association,
 *          so a single map() reaches the Counterparty without a second
 *          repository round-trip.
 * WHY:     Establishes the Optional.map/orElseThrow discipline that Day 4's
 *          controllers reuse for 404 handling.
 * OBSERVE: A missing tradeRef throws NoSuchElementException naming the ref;
 *          grep for isPresent()/.get() in this method returns nothing.
 * ============================================================================
 */
@Service
public class TradeLookupService {

    private final TradeRepository tradeRepo;

    public TradeLookupService(TradeRepository tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    /**
     * Resolves the counterparty for a given trade reference.
     * @param tradeRef the trade's business reference (e.g. "EQT-20260603-0001").
     * @return the non-null {@link Counterparty} associated with that trade.
     * @throws NoSuchElementException if no trade with that reference exists.
     */
    public Counterparty counterpartyForTradeRef(String tradeRef) {
        return tradeRepo.findByTradeRef(tradeRef)
                .map(Trade::getCounterparty)
                .orElseThrow(() -> new NoSuchElementException(
                        "No counterparty resolvable for trade " + tradeRef));
    }
}
