package com.dbtraining.reconx.kafka;


import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



/**
 * Consumes trade-events independently
 * and stores immutable audit history.
 *
 * Consumer group:
 * audit-service
 *
 * This allows Kafka fan-out:
 * recon-service receives the event
 * audit-service receives the same event
 */
@Component
public class AuditEventConsumer {


    private static final Logger log =
            LoggerFactory.getLogger(AuditEventConsumer.class);


    private final AuditLogRepository auditRepo;


    public AuditEventConsumer(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }



    @KafkaListener(
            topics = "trade-events",
            groupId = "audit-service"
    )
    @Transactional
    public void onTradeEvent(TradeEvent event) {


        AuditLogEntry entry =
                new AuditLogEntry(
                        event.eventId().toString(),
                        event.tradeRef(),
                        event.eventType().name(),
                        event.timestamp(),
                        event.actor(),
                        event.before(),
                        event.after()
                );


        auditRepo.save(entry);


        log.debug(
                "Audit row persisted eventId={} tradeRef={}",
                event.eventId(),
                event.tradeRef()
        );
    }

}