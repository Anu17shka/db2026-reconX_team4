package com.dbtraining.reconx.kafka;


import com.dbtraining.reconx.dto.TradeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;



@Component
public class TradeEventProducer {


    private static final Logger log =
            LoggerFactory.getLogger(TradeEventProducer.class);



    private final KafkaTemplate<String,Object> kafkaTemplate;



    public TradeEventProducer(
            KafkaTemplate<String,Object> kafkaTemplate
    ){
        this.kafkaTemplate = kafkaTemplate;
    }



    public void publish(TradeEvent event){


        kafkaTemplate.send(
                KafkaTopicsConfig.TRADE_EVENTS,
                event.tradeRef(),
                event
        );


        log.info(
                "TradeEvent published partition topic={} tradeRef={}",
                KafkaTopicsConfig.TRADE_EVENTS,
                event.tradeRef()
        );

    }

}