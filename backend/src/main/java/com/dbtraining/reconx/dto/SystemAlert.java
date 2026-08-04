package com.dbtraining.reconx.dto;

public record SystemAlert(
        String severity,
        String code,
        String message
) {}