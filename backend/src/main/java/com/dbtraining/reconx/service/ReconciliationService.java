package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.observability.ReconMetrics;
import com.dbtraining.reconx.repository.ReconResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final ReconciliationEngine engine;
    private final ReconResultRepository repository;
    private final ReconMetrics reconMetrics;


    public ReconciliationService(ReconciliationEngine engine,
                                 ReconResultRepository repository,
                                 ReconMetrics reconMetrics) {

        this.engine = engine;
        this.repository = repository;
        this.reconMetrics = reconMetrics;
    }


    /**
     * Runs reconciliation and records execution duration.
     *
     * Timer.record(Supplier<T>) automatically:
     *  - starts timer
     *  - executes engine.reconcile(...)
     *  - records elapsed time
     *  - returns original result
     */
    public List<ReconResult> runRecon(List<TradeType> internal,
                                      List<TradeType> external,
                                      ReconciliationRule rule) {


        List<ReconResult> results =
                reconMetrics.reconciliationTimer()
                        .record(() ->
                                engine.reconcile(
                                        internal,
                                        external,
                                        rule
                                )
                        );


        results.forEach(repository::save);

        return results;
    }
}