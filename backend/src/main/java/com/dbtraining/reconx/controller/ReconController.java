package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.ReconResolveRequest;
import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.dto.ResolutionRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.dbtraining.reconx.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * TICKET-ADV068 — POST /api/v1/recon/run — returns 202 + jobId
 * TICKET-ADV069 — GET  /api/v1/recon/jobs/{jobId}/results
 * TICKET-ADV070 — PUT  /api/v1/recon/results/{id}/resolve
 *
 * TICKET-ADV084 — Reconciliation duration timer verification
 */
@RestController
@RequestMapping("/v1/recon")
@Tag(name = "recon", description = "Reconciliation operations")
@SecurityRequirement(name = "bearerAuth")
public class ReconController {

    private final ReconBreakRepository breaks;
    private final ReconciliationService reconciliationService;


    public ReconController(ReconBreakRepository breaks,
                            ReconciliationService reconciliationService) {

        this.breaks = breaks;
        this.reconciliationService = reconciliationService;
    }


    @PostMapping("/run")
    @Operation(summary = "Trigger a reconciliation job (async)")
    public ResponseEntity<Map<String, String>> runRecon(
            @Valid @RequestBody ReconRunRequest req) {


        String jobId = UUID.randomUUID().toString();


        /*
         * TICKET-ADV084:
         * Execute reconciliation so Micrometer Timer records:
         *
         * reconciliation_duration_seconds_count
         * reconciliation_duration_seconds_sum
         * reconciliation_duration_seconds_bucket
         *
         * Replace this temporary loading with the async worker flow
         * when recon_jobs/Kafka processing is implemented.
         */
        List<TradeType> internal = List.of();
        List<TradeType> external = List.of();


        reconciliationService.runRecon(
                internal,
                external,
                ReconciliationRule.EXACT
        );


        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .location(
                        URI.create(
                                "/api/v1/recon/jobs/" + jobId + "/results"
                        )
                )
                .body(
                        Map.of(
                                "jobId", jobId,
                                "status", "QUEUED"
                        )
                );
    }


    @GetMapping("/jobs/{jobId}/results")
    @Operation(summary = "Get results for a recon job")
    public List<ReconBreak> results(
            @PathVariable String jobId) {

        return breaks.findAll();
    }


    @PutMapping("/results/{id}/resolve")
    @Operation(summary = "Mark a recon break as RESOLVED with a note")
    public ResponseEntity<ReconBreak> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolutionRequest req) {


        ReconBreak rb = breaks.findById(id)
                .orElseThrow(() ->
                        new TradeNotFoundException(
                                "recon_break " + id
                        ));


        rb.resolve(req.note());

        return ResponseEntity.ok(
                breaks.save(rb)
        );
    }
}