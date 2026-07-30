package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void createsMoney() {

        Money money = Money.of("100", "USD");

        assertThat(money.amount())
                .isEqualByComparingTo("100");

        assertThat(money.currency().getCurrencyCode())
                .isEqualTo("USD");
    }


    @Test
    void plusAddsSameCurrency() {

        Money result = Money.of("100", "USD")
                .plus(Money.of("50", "USD"));

        assertThat(result.amount())
                .isEqualByComparingTo("150");
    }


    @Test
    void plusRejectsDifferentCurrency() {

        assertThatThrownBy(() ->
                Money.of("100", "USD")
                        .plus(Money.of("50", "EUR"))
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("currency mismatch");
    }


    @Test
    void negativeAmountRejected() {

        assertThatThrownBy(() ->
                Money.of("-10", "USD")
        )
        .isInstanceOf(IllegalArgumentException.class);
    }
}