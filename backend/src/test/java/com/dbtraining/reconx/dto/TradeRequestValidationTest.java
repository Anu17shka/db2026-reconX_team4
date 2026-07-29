package com.dbtraining.reconx.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TradeRequestValidationTest {

    private final Validator validator;

    TradeRequestValidationTest() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }


    private TradeRequest validRequest() {
        return new TradeRequest(
                "ABC-20260603-0001",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("200"),
                LocalDate.of(2026, 6, 3)
        );
    }


    @Test
    void validRequest_returnsZeroViolations() {

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }


    @Test
    void negativeQuantity_returnsQuantityViolation() {

        TradeRequest request = new TradeRequest(
                "ABC-20260603-0001",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("-1"),
                new BigDecimal("200"),
                LocalDate.of(2026, 6, 3)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);

        assertThat(
                violations.iterator().next().getPropertyPath().toString()
        ).isEqualTo("quantity");
    }


    @Test
    void invalidTradeRef_returnsPatternViolation() {

        TradeRequest request = new TradeRequest(
                "foo",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("200"),
                LocalDate.of(2026, 6, 3)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .anyMatch(v ->
                        v.getMessage()
                        .equals("tradeRef must match AAA-YYYYMMDD-NNNN")
                );
    }
}