package com.dbtraining.reconx.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV084 — Reconciliation Duration Timer
 *
 * WHAT:
 *      Measures how long the reconciliation engine takes per batch.
 *
 * WHY:
 *      publishPercentileHistogram() creates Prometheus histogram buckets,
 *      allowing server-side percentile queries:
 *
 *      histogram_quantile(0.95, ...)
 *
 * PROMETHEUS OUTPUT:
 *
 *      reconciliation_duration_seconds_count
 *      reconciliation_duration_seconds_sum
 *      reconciliation_duration_seconds_bucket{le="..."}
 *
 * ============================================================================
 */
@Component
public class ReconMetrics {

    private final Timer reconciliationTimer;

    public ReconMetrics(MeterRegistry meterRegistry) {

        this.reconciliationTimer = Timer.builder("reconciliation.duration")
                .description("Duration of reconciliation engine execution")
                .publishPercentileHistogram()
                .publishPercentiles(0.50, 0.95, 0.99)
                .register(meterRegistry);
    }

    public Timer reconciliationTimer() {
        return reconciliationTimer;
    }
}