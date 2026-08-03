package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.ReconResolveRequest;
import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TICKET-ADV068 — POST /api/v1/recon/run — returns 202 + jobId
 * TICKET-ADV069 — GET  /api/v1/recon/jobs/{jobId}/results
 * TICKET-ADV070 — PUT  /api/v1/recon/results/{id}/resolve
 */
@RestController
@RequestMapping("/v1/recon")
@Tag(name = "recon", description = "Reconciliation operations")
@SecurityRequirement(name = "bearerAuth")
public class ReconController {

    private final ReconBreakRepository breaks;

    public ReconController(ReconBreakRepository breaks) { this.breaks = breaks; }

    @PostMapping("/run")
    @Operation(summary = "Trigger a reconciliation job (async)")
    public ResponseEntity<Map<String, String>> runRecon(@Valid @RequestBody ReconRunRequest req) {
        // In the full implementation this writes a row to recon_jobs and a
        // Kafka worker (Day 6) picks it up asynchronously.
        String jobId = UUID.randomUUID().toString();
        return ResponseEntity
                .accepted()
                .location(URI.create("/api/v1/recon/jobs/" + jobId + "/results"))
                .body(Map.of("jobId", jobId, "status", "QUEUED"));
    }

    @GetMapping("/jobs/{jobId}/results")
    @Operation(summary = "Get results for a recon job")
    public List<ReconBreak> results(@PathVariable String jobId) {
        // Trainer-stub: recon_jobs is not wired yet, so this returns every
        // currently open break regardless of jobId.
        return breaks.findAll();
    }

    @PutMapping("/results/{id}/resolve")
    @Operation(summary = "Mark a recon break as RESOLVED with a note")
    public ResponseEntity<ReconBreak> resolve(@PathVariable Long id,
                                              @Valid @RequestBody ReconResolveRequest body) {
        ReconBreak rb = breaks.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("recon_break " + id));
        rb.resolve(body.note());
        return ResponseEntity.ok(breaks.save(rb));
    }
}
