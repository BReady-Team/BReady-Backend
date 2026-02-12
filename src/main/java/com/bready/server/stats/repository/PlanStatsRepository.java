package com.bready.server.stats.repository;

import com.bready.server.stats.domain.PlanStats;
import com.bready.server.stats.domain.StatsPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanStatsRepository extends JpaRepository<PlanStats, Long> {
    List<PlanStats> findByPlanIdIn(List<Long> planIds);

    Optional<PlanStats> findByPlanIdAndPeriod(
            Long planId,
            StatsPeriod period
    );

    List<PlanStats> findByPeriodAndPlanIdIn(
            StatsPeriod period,
            List<Long> planIds
    );
}
