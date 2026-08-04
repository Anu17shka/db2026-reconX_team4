package com.dbtraining.reconx.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka payload for trade-events topic.
 */
public record TradeEvent(
        UUID eventId,
        String tradeRef,
        EventType eventType,
        Instant timestamp,
        String actor,
        String before,
        String after
) {

    public enum EventType {
        TRADE_CREATED,
        TRADE_UPDATED,
        TRADE_CANCELLED
    }


    public static TradeEvent created(
            String tradeRef,
            String after
    ) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_CREATED,
                Instant.now(),
                "system",
                null,
                after
        );
    }


    public static TradeEvent updated(
            String tradeRef,
            String before,
            String after
    ) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_UPDATED,
                Instant.now(),
                "system",
                before,
                after
        );
    }


    public static TradeEvent cancelled(
            String tradeRef,
            String before
    ) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_CANCELLED,
                Instant.now(),
                "system",
                before,
                null
        );
    }
}