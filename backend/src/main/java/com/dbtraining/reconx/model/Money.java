package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Money amount cannot be negative: " + amount);
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(
                new BigDecimal(amount),
                Currency.getInstance(currencyCode)
        );
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(
                amount,
                Currency.getInstance(currencyCode)
        );
    }

    public Money plus(Money other) {

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "currency mismatch");
        }

        return new Money(
                this.amount.add(other.amount),
                this.currency
        );
    }
}