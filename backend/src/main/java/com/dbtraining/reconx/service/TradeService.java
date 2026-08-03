package com.dbtraining.reconx.service;


import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;

import com.dbtraining.reconx.exception.DuplicateTradeRefException;

import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.TradeSpecification;

import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;



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



    public Trade create(
            TradeRequest req,
            String actor
    ) {

        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
            throw new DuplicateTradeRefException(
                    "Trade already exists with tradeRef " + req.tradeRef());
        }

        Counterparty counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Counterparty not found with id " + req.counterpartyId()));

        Instrument instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Instrument not found with id " + req.instrumentId()));

        Trade trade = new Trade();
        trade.setTradeRef(req.tradeRef());
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setAssetClass(instrument.getAssetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());
        trade.setStatus("PENDING");

        Trade saved = tradeRepo.save(trade);

        metrics.incrementTradeCreated();
        metrics.recordTradeValue(req.quantity().multiply(req.price()).doubleValue());

        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_CREATED, Instant.now(), actor, null, null));

        return saved;
    }




    public Trade update(
            Long id,
            TradeRequest req,
            String actor
    ) {

        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found with id " + id));

        Counterparty counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Counterparty not found with id " + req.counterpartyId()));

        Instrument instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Instrument not found with id " + req.instrumentId()));

        trade.setTradeRef(req.tradeRef());
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setAssetClass(instrument.getAssetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());

        Trade saved = tradeRepo.save(trade);

        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_UPDATED, Instant.now(), actor, null, null));

        return saved;
    }




    public Trade updateStatus(
            Long id,
            String status,
            String actor
    ) {

        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found with id " + id));

        trade.setStatus(status);

        Trade saved = tradeRepo.save(trade);

        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_UPDATED, Instant.now(), actor, null, null));

        return saved;
    }





    public void softDelete(
            Long id,
            String actor
    ) {

        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found with id " + id));

        trade.softDelete();
        tradeRepo.save(trade);

        events.publish(new TradeEvent(UUID.randomUUID(), trade.getTradeRef(),
                TradeEvent.EventType.TRADE_CANCELLED, Instant.now(), actor, null, null));
    }





    // TICKET-ADV062
    @Transactional(readOnly = true)
    public Trade getById(Long id) {


        return tradeRepo.findById(id)

                .orElseThrow(() ->
                        new TradeNotFoundException(
                                "Trade not found with id " + id
                        )
                );
    }





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
                        .where(
                            TradeSpecification.tradeDateBetween(from,to)
                        )
                        .and(
                            TradeSpecification.hasStatus(status)
                        )
                        .and(
                            TradeSpecification.forCounterparty(counterpartyId)
                        );


        return tradeRepo.findAll(
                spec,
                pageable
        );
    }

}