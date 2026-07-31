package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeLookupServiceTest {

    @Mock
    private TradeRepository tradeRepo;

    @InjectMocks
    private TradeLookupService lookupService;

    @Test
    void counterpartyForTradeRef_tradeFound_returnsCounterparty() {

        Trade trade = new Trade();
        Counterparty counterparty = new Counterparty();
        trade.setCounterparty(counterparty);

        when(tradeRepo.findByTradeRef("EQT-20260603-0001"))
                .thenReturn(Optional.of(trade));

        Counterparty result = lookupService.counterpartyForTradeRef("EQT-20260603-0001");

        assertThat(result).isSameAs(counterparty);
    }


    @Test
    void counterpartyForTradeRef_tradeMissing_throwsWithTradeRefInMessage() {

        when(tradeRepo.findByTradeRef("MISSING-0001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookupService.counterpartyForTradeRef("MISSING-0001"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("MISSING-0001");
    }
}
