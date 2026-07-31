package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV055 smoke test. Queries the Day 1 Liquibase seed data directly
 * (trades.csv, 500 rows) rather than inserting new rows -- the seed uses
 * explicit primary keys via Liquibase's loadData, and H2's IDENTITY
 * generator does not advance past explicitly-inserted ids, so any INSERT
 * through JPA collides with the next seeded row. Reading the seed instead
 * sidesteps that entirely and matches the ticket's own verify step
 * ("against the Day 1 seed").
 *
 * Pins the H2 URL to the same MODE=PostgreSQL variant used elsewhere in
 * this project instead of DataJpaTest's default anonymous embedded DB, to
 * avoid the CLOB/TEXT schema-validation mismatch on audit_log that plain
 * H2 mode triggers against this schema.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:trade-repo-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void findByFilters_returnsSeedTradesInsideDateRange() {
        Page<Trade> results = tradeRepository.findByFilters(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 20),
                null,
                null,
                PageRequest.of(0, 200));

        assertThat(results.getTotalElements()).isEqualTo(125);
        assertThat(results.getContent())
                .allSatisfy(t -> assertThat(t.getTradeDate())
                        .isBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20)));
    }

    @Test
    void findByFilters_outsideDateRange_returnsEmpty() {
        Page<Trade> results = tradeRepository.findByFilters(
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 31),
                null,
                null,
                PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
    }

    @Test
    void findByFilters_filtersByStatus() {
        Page<Trade> results = tradeRepository.findByFilters(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20),
                "MATCHED", null, PageRequest.of(0, 200));

        assertThat(results.getTotalElements()).isEqualTo(31);
        assertThat(results.getContent())
                .allSatisfy(t -> assertThat(t.getStatus()).isEqualTo("MATCHED"));
    }

    @Test
    void findByFilters_filtersByCounterpartyId() {
        Page<Trade> results = tradeRepository.findByFilters(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20),
                null, 4L, PageRequest.of(0, 200));

        assertThat(results.getTotalElements()).isEqualTo(13);
        assertThat(results.getContent())
                .allSatisfy(t -> assertThat(t.getCounterparty().getId()).isEqualTo(4L));
    }
}
