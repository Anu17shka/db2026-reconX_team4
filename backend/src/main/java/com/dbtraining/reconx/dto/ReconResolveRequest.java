package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** TICKET-ADV070 — PUT /v1/recon/results/{id}/resolve body. */
public record ReconResolveRequest(

        @NotBlank
        @Size(max = 500)
        String note

) {}
