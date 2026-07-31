package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.TradeSpecification;
import com.dbtraining.reconx.repository.entity.Trade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;


    public TradeService(
            TradeRepository tradeRepo,
            CounterpartyRepository cpRepo,
            InstrumentRepository instRepo,
            TradeEventProducer events,
            TradeMetrics metrics
    ) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }


    public Trade create(TradeRequest req, String actor) {

        throw new UnsupportedOperationException("TICKET-ADV064");
    }


    public Trade update(Long id, TradeRequest req, String actor) {

        throw new UnsupportedOperationException("TICKET-ADV065");
    }


    public Trade updateStatus(Long id, String status, String actor) {

        throw new UnsupportedOperationException("TICKET-ADV066");
    }


    public void softDelete(Long id, String actor) {

        throw new UnsupportedOperationException("TICKET-ADV067");
    }


    /**
     * Dynamic trade search using JPA Specifications.
     *
     * @param from start trade date filter
     * @param to end trade date filter
     * @param status trade status filter
     * @param counterpartyId counterparty filter
     * @param pageable pagination information
     * @return filtered paginated trades
     */
    @Transactional(readOnly = true)
    public Page<Trade> list(
            LocalDate from,
            LocalDate to,
            String status,
            Long counterpartyId,
            Pageable pageable
    ) {

        Specification<Trade> spec =
                Specification
                        .where(TradeSpecification.tradeDateBetween(from, to))
                        .and(TradeSpecification.hasStatus(status))
                        .and(TradeSpecification.forCounterparty(counterpartyId));


        return tradeRepo.findAll(spec, pageable);
    }
}