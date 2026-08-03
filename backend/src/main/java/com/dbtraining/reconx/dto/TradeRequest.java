// package com.dbtraining.reconx.dto;

// import jakarta.validation.constraints.*;

// import java.math.BigDecimal;
// import java.time.LocalDate;

// /**
//  * ============================================================================
//  * TICKET-ADV053 — TradeRequest DTO (POST body)
//  * TICKET-ADV029 — JSR-380 validation annotations live on the DTO, not the entity
//  *
//  * WHY:    Putting @Pattern/@Positive/@NotNull on the JPA entity couples
//  *         persistence to wire format. The DTO is the wire contract; validate
//  *         it before mapping.
//  * ============================================================================
//  */
// public record TradeRequest(
//         @NotNull
//         @Pattern(regexp = "^[A-Z]{3}-\\d{8}-\\d{4}$",
//                  message = "tradeRef must match AAA-YYYYMMDD-NNNN")
//         String tradeRef,

//         @NotNull
//         Long instrumentId,

//         @NotNull
//         Long counterpartyId,

//         @NotBlank
//         String assetClass,

//         @NotBlank
//         @Pattern(regexp = "^(BUY|SELL)$")
//         String side,

//         @NotNull @Positive
//         BigDecimal quantity,

//         @NotNull @PositiveOrZero
//         BigDecimal price,

//         @NotNull
//         LocalDate tradeDate
// ) {}


package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO used for creating a trade.
 *
 * <p>This record represents the wire format accepted by the API.
 * It contains only primitive/JDK types and does not expose JPA entities.</p>
 */
public record TradeRequest(

        @NotBlank
        String tradeRef,

        @NotNull
        Long counterpartyId,

        @NotNull
        Long instrumentId,

        @NotBlank
        @Pattern(regexp = "^(BUY|SELL)$")
        String side,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotNull
        LocalDate tradeDate

) {
}