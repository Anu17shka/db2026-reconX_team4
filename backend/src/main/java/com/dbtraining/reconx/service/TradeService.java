package com.dbtraining.reconx.service;


import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;

import com.dbtraining.reconx.exception.DuplicateTradeRefException;

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

import java.time.LocalDate;



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

        throw new UnsupportedOperationException("TICKET-ADV064");
    }




    public Trade update(
            Long id,
            TradeRequest req,
            String actor
    ) {

        throw new UnsupportedOperationException("TICKET-ADV065");
    }




    public Trade updateStatus(
            Long id,
            String status,
            String actor
    ) {

        throw new UnsupportedOperationException("TICKET-ADV066");
    }





    public void softDelete(
            Long id,
            String actor
    ) {

        throw new UnsupportedOperationException("TICKET-ADV067");
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