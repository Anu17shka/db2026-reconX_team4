package com.dbtraining.reconx.service;


import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Counterparty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
public class TradeService {


    private static final Logger log =
            LoggerFactory.getLogger(TradeService.class);



    private final TradeRepository tradeRepository;

    private final TradeEventProducer tradeEventProducer;

    private final ObjectMapper objectMapper;

    private final InstrumentRepository instrumentRepository;

    private final CounterpartyRepository counterpartyRepository;



    public TradeService(
            TradeRepository tradeRepository,
            TradeEventProducer tradeEventProducer,
            ObjectMapper objectMapper,
            InstrumentRepository instrumentRepository,
            CounterpartyRepository counterpartyRepository
    ) {

        this.tradeRepository = tradeRepository;
        this.tradeEventProducer = tradeEventProducer;
        this.objectMapper = objectMapper;
        this.instrumentRepository = instrumentRepository;
        this.counterpartyRepository = counterpartyRepository;

    }




    public Page<Trade> list(
            LocalDate from,
            LocalDate to,
            String status,
            Long counterpartyId,
            Pageable pageable
    ) {


        if (from == null) {
            from = LocalDate.of(2000,1,1);
        }


        if (to == null) {
            to = LocalDate.now();
        }


        return tradeRepository.findByFilters(
                from,
                to,
                status,
                counterpartyId,
                pageable
        );

    }





    public Trade getById(Long id) {

        return tradeRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Trade not found: " + id
                        )
                );
    }





    @Transactional
    public Trade create(
            TradeRequest request,
            String actor
    ) {


        Trade trade = new Trade();


        trade.setTradeRef(
                request.tradeRef()
        );


        trade.setSide(
                request.side()
        );


        trade.setQuantity(
                request.quantity()
        );


        trade.setPrice(
                request.price()
        );


        trade.setTradeDate(
                request.tradeDate()
        );


        trade.setAssetClass(
                "FX"
        );


        trade.setStatus(
                "PENDING"
        );



        Instrument instrument =
                instrumentRepository.findById(
                        request.instrumentId()
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Instrument not found"
                        )
                );



        Counterparty counterparty =
                counterpartyRepository.findById(
                        request.counterpartyId()
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Counterparty not found"
                        )
                );



        trade.setInstrument(instrument);

        trade.setCounterparty(counterparty);



        Trade saved =
                tradeRepository.save(trade);



        publishCreated(saved, actor);



        return saved;

    }





    @Transactional
    public Trade update(
            Long id,
            TradeRequest request,
            String actor
    ) {


        Trade trade = getById(id);


        trade.setTradeRef(
                request.tradeRef()
        );


        trade.setSide(
                request.side()
        );


        trade.setQuantity(
                request.quantity()
        );


        trade.setPrice(
                request.price()
        );


        trade.setTradeDate(
                request.tradeDate()
        );


        trade.setInstrument(
                instrumentRepository.findById(
                        request.instrumentId()
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Instrument not found"
                        )
                )
        );


        trade.setCounterparty(
                counterpartyRepository.findById(
                        request.counterpartyId()
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Counterparty not found"
                        )
                )
        );



        Trade saved =
                tradeRepository.save(trade);



        publishUpdated(saved, actor);



        return saved;

    }





    @Transactional
    public Trade updateStatus(
            Long id,
            String status,
            String actor
    ) {


        Trade trade = getById(id);


        trade.setStatus(status);


        Trade saved =
                tradeRepository.save(trade);


        publishUpdated(saved, actor);


        return saved;

    }





    @Transactional
    public void softDelete(
            Long id,
            String actor
    ) {


        Trade trade = getById(id);


        publishCancelled(
                trade,
                actor
        );


        trade.softDelete();


        tradeRepository.save(trade);

    }





    private void publishCreated(
            Trade trade,
            String actor
    ) {

        try {

            String after =
                    objectMapper.writeValueAsString(trade);



            TradeEvent event =
                    TradeEvent.created(
                            trade.getTradeRef(),
                            after
                    );


            tradeEventProducer.publish(event);



            log.info(
                    "TRADE_CREATED published {}",
                    trade.getTradeRef()
            );


        } catch(JsonProcessingException e) {

            log.error(
                    "Failed publishing create event",
                    e
            );

        }

    }





    private void publishUpdated(
            Trade trade,
            String actor
    ) {

        try {

            String after =
                    objectMapper.writeValueAsString(trade);



            TradeEvent event =
                    TradeEvent.updated(
                            trade.getTradeRef(),
                            null,
                            after
                    );


            tradeEventProducer.publish(event);



        } catch(JsonProcessingException e) {

            log.error(
                    "Failed publishing update event",
                    e
            );

        }

    }





    private void publishCancelled(
            Trade trade,
            String actor
    ) {

        try {

            String before =
                    objectMapper.writeValueAsString(trade);



            TradeEvent event =
                    TradeEvent.cancelled(
                            trade.getTradeRef(),
                            before
                    );


            tradeEventProducer.publish(event);



        } catch(JsonProcessingException e) {

            log.error(
                    "Failed publishing cancel event",
                    e
            );

        }

    }

}