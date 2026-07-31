package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * TICKET-ADV056 — Dynamic specifications for Trade filtering.
 *
 * Provides reusable predicates that can be composed using
 * Specification.where(...).and(...).
 */
public final class TradeSpecification {

    private TradeSpecification() {
    }


    public static Specification<Trade> tradeDateBetween(
            LocalDate from,
            LocalDate to
    ) {

        return (root, query, cb) -> {

            if (from == null && to == null) {
                return cb.conjunction();
            }

            if (from == null) {
                return cb.lessThanOrEqualTo(
                        root.get("tradeDate"),
                        to
                );
            }

            if (to == null) {
                return cb.greaterThanOrEqualTo(
                        root.get("tradeDate"),
                        from
                );
            }

            return cb.between(
                    root.get("tradeDate"),
                    from,
                    to
            );
        };
    }


    public static Specification<Trade> hasStatus(
            String status
    ) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }


    public static Specification<Trade> forCounterparty(
            Long counterpartyId
    ) {

        return (root, query, cb) -> {

            if (counterpartyId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("counterparty").get("id"),
                    counterpartyId
            );
        };
    }


    public static Specification<Trade> refLike(
            String pattern
    ) {

        return (root, query, cb) -> {

            if (pattern == null || pattern.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    root.get("tradeRef"),
                    pattern + "%"
            );
        };
    }
}