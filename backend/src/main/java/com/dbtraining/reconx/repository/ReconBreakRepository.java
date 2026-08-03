package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.ReconBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconBreakRepository extends JpaRepository<ReconBreak, Long> {

    long countByStatus(String status);

}