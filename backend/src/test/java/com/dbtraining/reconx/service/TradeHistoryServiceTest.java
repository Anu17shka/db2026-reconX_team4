package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV052 smoke test. Envers ties a revision to a committed
 * transaction, not to an individual save() call -- if the whole test body
 * ran inside DataJpaTest's default rollback-wrapped transaction, all three
 * updates would collapse into a single revision. Propagation.NOT_SUPPORTED
 * disables that wrapping so each repository call commits independently,
 * matching how updates actually happen outside a test.
 *
 * Pins the H2 URL to the same MODE=PostgreSQL variant used elsewhere in
 * this project (and a name distinct from other @DataJpaTest classes, since
 * this test doesn't roll back and shouldn't share state with them).
 */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TradeHistoryService.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:trade-history-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TradeHistoryServiceTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeHistoryService tradeHistoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * The Day 1 seed inserts trades with explicit ids via Liquibase's
     * loadData, which H2's IDENTITY generator doesn't advance past --
     * without this, the first save() below collides with a seeded row.
     */
    @BeforeEach
    void bumpIdentityPastSeedData() {
        jdbcTemplate.execute("ALTER TABLE trades ALTER COLUMN id RESTART WITH 100000");
    }

    @Test
    void threeUpdates_produceFourRevisions() {
        var counterparty = counterpartyRepository.findAll().get(0);
        var instrument = instrumentRepository.findAll().get(0);

        Trade trade = new Trade();
        trade.setTradeRef("ENV-20260603-0001");
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setAssetClass("EQUITY");
        trade.setSide("BUY");
        trade.setQuantity(new BigDecimal("100"));
        trade.setPrice(new BigDecimal("100.50"));
        trade.setTradeDate(LocalDate.of(2026, 6, 3));
        trade.setStatus("PENDING");
        trade = tradeRepository.save(trade);           // revision 1 (insert)

        trade.setStatus("CONFIRMED");
        trade = tradeRepository.save(trade);            // revision 2 (update)

        trade.setStatus("MATCHED");
        trade = tradeRepository.save(trade);            // revision 3 (update)

        trade.setStatus("SETTLED");
        trade = tradeRepository.save(trade);            // revision 4 (update)

        var revisions = tradeHistoryService.revisionsFor(trade.getId());
        assertThat(revisions).hasSize(4);

        Trade firstSnapshot = tradeHistoryService.snapshotAt(trade.getId(), revisions.get(0));
        assertThat(firstSnapshot.getStatus()).isEqualTo("PENDING");

        Trade lastSnapshot = tradeHistoryService.snapshotAt(trade.getId(), revisions.get(3));
        assertThat(lastSnapshot.getStatus()).isEqualTo("SETTLED");
    }
}
