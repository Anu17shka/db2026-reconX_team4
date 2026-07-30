package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Placeholder implementation: no `recon_results` table or entity exists yet
 * in this schema (recon_breaks/recon_jobs cover break-specific persistence;
 * see ADV069/ADV070). Without any concrete bean, ReconciliationService fails
 * to wire up at boot -- this logs the result so the app can actually start
 * until the real persistence story for ReconResult is decided.
 */
@Repository
public class ReconResultRepositoryImpl implements ReconResultRepository {

    private static final Logger log = LoggerFactory.getLogger(ReconResultRepositoryImpl.class);

    @Override
    public void save(ReconResult result) {
        log.info("Recon result: tradeRef={} status={} discrepancyType={} details={}",
                result.tradeRef(), result.status(), result.discrepancyType(), result.details());
    }
}
