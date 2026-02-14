package com.bready.server.stats.repository;

import com.bready.server.stats.domain.PlanStats;
import com.bready.server.stats.domain.StatsPeriod;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanStatsRepository extends JpaRepository<PlanStats, Long> {
    Optional<PlanStats> findByPlanIdAndPeriod(
            Long planId,
            StatsPeriod period
    );

    List<PlanStats> findByPeriodAndPlanIdIn(
            StatsPeriod period,
            List<Long> planIds
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ps
        from PlanStats ps
        where ps.planId = :planId
          and ps.period = :period
    """)
    Optional<PlanStats> findByPlanIdAndPeriodForUpdate(
            @Param("planId") Long planId,
            @Param("period") StatsPeriod period
    );
}
