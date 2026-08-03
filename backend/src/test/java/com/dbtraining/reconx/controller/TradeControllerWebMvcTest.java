package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapperImpl;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.security.JwtTokenProvider;
import com.dbtraining.reconx.security.SecurityConfig;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TICKET-ADV075 — authenticated create returns 201
 * TICKET-ADV076 — unauthenticated create returns 401
 * TICKET-ADV077 — VIEWER role returns 403
 *
 * TradeMapperImpl is imported explicitly: @WebMvcTest's slice filter only
 * picks up web-layer beans (controllers/advice/converters/filters), so the
 * MapStruct-generated mapper — a plain @Component — would otherwise be
 * missing from the context even though TradeService is mocked. SecurityConfig
 * is a plain @Configuration (not a controller/filter/converter), so the slice
 * excludes it too — without importing it explicitly, JwtAuthenticationFilter
 * still gets auto-registered as a raw servlet Filter (Boot registers any
 * Filter bean it finds), but Spring Security falls back to its own default
 * chain instead of our RBAC rules, which is why the 401/403 boundary needs
 * the real SecurityFilterChain bean present. JwtTokenProvider is SecurityConfig's
 * transitive @Component dependency (via JwtAuthenticationFilter) and needs the
 * same explicit import.
 */
@WebMvcTest(TradeController.class)
@Import({TradeMapperImpl.class, JwtTokenProvider.class, SecurityConfig.class})
class TradeControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TradeService tradeService;

    private TradeRequest validRequest() {
        return new TradeRequest(
                "TRD-20260315-9999",
                1L,
                1L,
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now());
    }

    // Trade/Counterparty/Instrument expose no id setters (IDENTITY-generated),
    // so the mocked service's return value is built via ReflectionTestUtils.
    private Trade mockSavedTrade() {
        Counterparty cp = new Counterparty();
        ReflectionTestUtils.setField(cp, "id", 1L);
        cp.setName("Apex Brokers Inc");

        Instrument inst = new Instrument();
        ReflectionTestUtils.setField(inst, "id", 1L);
        ReflectionTestUtils.setField(inst, "symbol", "SAP.DE");
        ReflectionTestUtils.setField(inst, "name", "SAP SE");
        ReflectionTestUtils.setField(inst, "assetClass", "EQUITY");
        ReflectionTestUtils.setField(inst, "currency", "EUR");

        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "id", 42L);
        trade.setTradeRef("TRD-20260315-9999");
        trade.setCounterparty(cp);
        trade.setInstrument(inst);
        trade.setAssetClass("EQUITY");
        trade.setSide("BUY");
        trade.setQuantity(new BigDecimal("100.0000"));
        trade.setPrice(new BigDecimal("245.50"));
        trade.setTradeDate(LocalDate.now());
        trade.setStatus("PENDING");
        return trade;
    }

    @Test
    @WithMockUser(roles = "TRADER")
    void testCreateTrade_authenticated_returns201() throws Exception {
        when(tradeService.create(any(), any())).thenReturn(mockSavedTrade());

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/trades/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
    }

    @Test
    void testCreateTrade_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testCreateTrade_viewerRole_returns403() throws Exception {
        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
