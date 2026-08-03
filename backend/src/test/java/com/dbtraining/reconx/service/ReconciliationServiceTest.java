package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import com.dbtraining.reconx.observability.ReconMetrics;
import com.dbtraining.reconx.repository.ReconResultRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReconciliationServiceTest {

    @Test
    void testReconcile_savesResultWithMatchedStatus() {

        // given
        ReconResultRepository repo = mock(ReconResultRepository.class);

        ReconciliationEngine engine = new ReconciliationEngine();

        ReconMetrics reconMetrics =
                new ReconMetrics(new SimpleMeterRegistry());

        ReconciliationService service =
                new ReconciliationService(
                        engine,
                        repo,
                        reconMetrics
                );


        List<TradeType> internal =
                List.of(
                        equity(
                                "EQU-20260603-0001",
                                "100.00",
                                "10"
                        )
                );

        List<TradeType> external =
                List.of(
                        equity(
                                "EQU-20260603-0001",
                                "100.00",
                                "10"
                        )
                );


        // when
        service.runRecon(
                internal,
                external,
                ReconciliationRule.EXACT
        );


        // then
        ArgumentCaptor<ReconResult> captor =
                ArgumentCaptor.forClass(ReconResult.class);

        verify(repo).save(captor.capture());

        ReconResult saved = captor.getValue();

        assertThat(saved.tradeRef())
                .isEqualTo("EQU-20260603-0001");

        assertThat(saved.status())
                .isEqualTo(ReconResult.Status.MATCHED);
    }


    private EquityTrade equity(String ref,
                               String price,
                               String qty) {

        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}