package com.dbtraining.reconx.integration;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * TICKET-ADV078 — full trade lifecycle over real HTTP against a real Postgres.
 *
 * Adapted from the guide's reference to the actual DTO shapes in this repo:
 * LoginRequest's field is {@code email} (not {@code username}), and
 * TradeRequest carries {@code side} + swapped counterparty/instrument order.
 * recon_breaks has no seed data (006-audit-and-recon.xml creates the table
 * empty), so the break resolved in step 6 is inserted directly via the
 * repository rather than assumed to pre-exist at id=1.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradeLifecycleIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort int port;
    @Autowired ObjectMapper om;
    @Autowired ReconBreakRepository breakRepo;

    static String token;
    static Long createdId;
    static String reconJobId;
    static Long breakId;

    // Plain RestTemplate() rejects PATCH (JDK's HttpURLConnection doesn't
    // support it); HttpComponentsClientHttpRequestFactory does.
    RestTemplate http = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);
        return h;
    }

    @Test @Order(1)
    void loginAsAdmin() {
        var body = """
                {"email":"admin@db.com","password":"admin123"}
                """;
        var req = new HttpEntity<>(body, new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
        }});
        var resp = http.postForEntity(
                "http://localhost:" + port + "/api/auth/login", req, JsonNode.class);
        Assertions.assertEquals(HttpStatus.OK, resp.getStatusCode());
        token = resp.getBody().get("token").asText();
        Assertions.assertNotNull(token);
    }

    @Test @Order(2)
    void createTrade() {
        // tradeRef must match no existing seed row; side/quantity/price/tradeDate
        // are the fields TradeRequest actually carries in this codebase.
        var body = """
                {"tradeRef":"INT-20260315-0001","instrumentId":1,"counterpartyId":1,
                 "side":"BUY","quantity":100.0,"price":245.50,"tradeDate":"2026-03-15"}
                """;
        var resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades",
                HttpMethod.POST, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        Assertions.assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        createdId = resp.getBody().get("id").asLong();
    }

    @Test @Order(3)
    void getTradeBack() {
        var resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades?status=PENDING",
                HttpMethod.GET, new HttpEntity<>(authHeaders()), JsonNode.class);
        Assertions.assertEquals(HttpStatus.OK, resp.getStatusCode());
        Assertions.assertTrue(resp.getBody().get("totalElements").asLong() >= 1);
    }

    @Test @Order(4)
    void patchStatus() {
        var body = """
                {"status":"MATCHED"}
                """;
        var resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades/" + createdId + "/status",
                HttpMethod.PATCH, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        Assertions.assertEquals(HttpStatus.OK, resp.getStatusCode());
        Assertions.assertEquals("MATCHED", resp.getBody().get("status").asText());
    }

    @Test @Order(5)
    void triggerRecon() {
        var body = """
                {"from":"2026-03-01","to":"2026-03-31"}
                """;
        var resp = http.exchange(
                "http://localhost:" + port + "/api/v1/recon/run",
                HttpMethod.POST, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        Assertions.assertEquals(HttpStatus.ACCEPTED, resp.getStatusCode());
        reconJobId = resp.getBody().get("jobId").asText();
        Assertions.assertNotNull(reconJobId);
    }

    @Test @Order(6)
    void resolveBreak() {
        // recon_breaks has no seed rows (the trainer-stub /recon/run doesn't
        // synchronously create any) — insert one directly so the resolve
        // endpoint has something real to act on.
        ReconBreak rb = new ReconBreak();
        rb.setTradeId(createdId);
        rb.setDiscrepancyType("PRICE_MISMATCH");
        breakId = breakRepo.save(rb).getId();

        var body = """
                {"note":"Confirmed via counterparty email on 2026-03-16."}
                """;
        var resp = http.exchange(
                "http://localhost:" + port + "/api/v1/recon/results/" + breakId + "/resolve",
                HttpMethod.PUT, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        Assertions.assertEquals(HttpStatus.OK, resp.getStatusCode());
        Assertions.assertEquals("RESOLVED", resp.getBody().get("status").asText());
    }
}
