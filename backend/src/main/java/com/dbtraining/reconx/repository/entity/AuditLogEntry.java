package com.dbtraining.reconx.repository.entity;


import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "audit_log")
public class AuditLogEntry {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;


    @Column(name = "trade_ref", nullable = false)
    private String tradeRef;


    @Column(name = "event_type", nullable = false)
    private String eventType;


    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;


    private String actor;


    @Column(
            name = "before_state",
            columnDefinition = "VARCHAR(1000000)"
    )
    private String beforeState;


    @Column(
            name = "after_state",
            columnDefinition = "VARCHAR(1000000)"
    )
    private String afterState;



    protected AuditLogEntry() {
    }



    public AuditLogEntry(
            String eventId,
            String tradeRef,
            String eventType,
            Instant eventTimestamp,
            String actor,
            String beforeState,
            String afterState
    ) {
        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
        this.actor = actor;
        this.beforeState = beforeState;
        this.afterState = afterState;
    }



    public Long getId() {
        return id;
    }


    public String getEventId() {
        return eventId;
    }


    public String getTradeRef() {
        return tradeRef;
    }


    public String getEventType() {
        return eventType;
    }


    public Instant getEventTimestamp() {
        return eventTimestamp;
    }


    public String getActor() {
        return actor;
    }


    public String getBeforeState() {
        return beforeState;
    }


    public String getAfterState() {
        return afterState;
    }
}