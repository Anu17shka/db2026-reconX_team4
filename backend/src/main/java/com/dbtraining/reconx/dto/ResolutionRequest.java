package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolutionRequest(

        @NotBlank
        @Size(max = 500)
        String note

) {}