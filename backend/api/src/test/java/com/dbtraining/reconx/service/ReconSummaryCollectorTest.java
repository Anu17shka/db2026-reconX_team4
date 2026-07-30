package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


class ReconSummaryCollectorTest {


    @Test
    void parallelStreamProducesSameSummaryAsSerial() {

        // Given
        List<ReconResult> results = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {

            if (i % 3 == 0) {
                results.add(
                        ReconResult.matched(
                                "EQU-20260730-" + String.format("%04d", i)
                        )
                );
            } else {
                results.add(
                        ReconResult.breakResult(
                                "EQU-20260730-" + String.format("%04d", i),
                                "VALUE_MISMATCH",
                                "test"
                        )
                );
            }
        }


        // When
        ReconSummary serial =
                results.stream()
                        .collect(new ReconSummaryCollector());


        ReconSummary parallel =
                results.parallelStream()
                        .collect(new ReconSummaryCollector());


        // Then
        assertThat(parallel.total())
                .isEqualTo(serial.total());

        assertThat(parallel.matched())
                .isEqualTo(serial.matched());

        assertThat(parallel.broken())
                .isEqualTo(serial.broken());
    }



    @Test
    void emptyFactoryReturnsZeroSummary() {

        // When
        ReconSummary summary =
                ReconSummary.empty();


        // Then
        assertThat(summary.total())
                .isZero();

        assertThat(summary.matched())
                .isZero();

        assertThat(summary.broken())
                .isZero();
    }



    @Test
    void summaryFieldsAreImmutable() {

        ReconSummary summary =
                new ReconSummary(
                        10,
                        7,
                        3
                );


        assertThat(summary.total())
                .isEqualTo(10);

        assertThat(summary.matched())
                .isEqualTo(7);

        assertThat(summary.broken())
                .isEqualTo(3);
    }
}