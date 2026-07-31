// package com.dbtraining.reconx.repository;

// import com.dbtraining.reconx.dto.ReconResult;

// public interface ReconResultRepository {

//     void save(ReconResult result);

// }


package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for storing reconciliation results.
 */
@Repository
public class ReconResultRepository {

    private final List<ReconResult> results = new ArrayList<>();


    public void save(ReconResult result) {
        results.add(result);
    }


    public List<ReconResult> findAll() {
        return results;
    }
}