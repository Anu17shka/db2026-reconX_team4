package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.entity.Trade;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================================
 * TICKET-ADV052 — Hibernate Envers query surface for Trade history
 *
 * WHAT:    Two read-only queries against the trades_aud/revinfo tables that
 *          Envers maintains automatically for every committed change to an
 *          @Audited Trade.
 * HOW:     AuditReaderFactory.get(em) wraps the same EntityManager the rest
 *          of the app uses; getRevisions/find delegate straight to Envers.
 * WHY:     Gives the compliance officer persona a tamper-evident trail
 *          without a single hand-written trigger.
 * ============================================================================
 */
@Service
public class TradeHistoryService {

    private final EntityManager em;

    public TradeHistoryService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public List<Number> revisionsFor(Long tradeId) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.getRevisions(Trade.class, tradeId);
    }

    @Transactional(readOnly = true)
    public Trade snapshotAt(Long tradeId, Number revision) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.find(Trade.class, tradeId, revision);
    }
}
