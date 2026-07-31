package com.dbtraining.reconx.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record TradeRef(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Z]{3}-\\d{8}-\\d{4}$");


    public TradeRef {

        Objects.requireNonNull(value, "tradeRef value");

        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid tradeRef format - expected AAA-YYYYMMDD-NNNN");
        }
    }


    public static TradeRef of(String value) {
        return new TradeRef(value);
    }


    @Override
    public String toString() {
        return value;
    }
}