package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TradeRefTest {


    @Test
    void validTradeRefCreated() {

        TradeRef ref =
                TradeRef.of("EQU-20260602-0001");

        assertThat(ref.value())
                .isEqualTo("EQU-20260602-0001");
    }


    @Test
    void invalidTradeRefRejected() {

        assertThatThrownBy(() ->
                TradeRef.of("foo")
        )
        .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void nullTradeRefRejected() {

        assertThatThrownBy(() ->
                TradeRef.of(null)
        )
        .isInstanceOf(NullPointerException.class);
    }
}