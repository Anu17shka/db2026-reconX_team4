package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** TICKET-ADV066 — PATCH /v1/trades/{id}/status body. */
public record TradeStatusUpdateRequest(

        @NotBlank
        @Pattern(regexp = "^(PENDING|MATCHED|UNMATCHED|DISPUTED|CANCELLED|RESOLVED)$",
                message = "status must be one of PENDING, MATCHED, UNMATCHED, DISPUTED, CANCELLED, RESOLVED")
        String status

) {}
