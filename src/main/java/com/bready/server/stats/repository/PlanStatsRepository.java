package com.bready.server.stats.repository;

import com.bready.server.stats.domain.PlanStats;
import com.bready.server.stats.domain.StatsPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanStatsRepository extends JpaRepository<PlanStats, Long> {
    List<PlanStats> findByPlanIdIn(List<Long> planIds);

    List<PlanStats> findByPeriodAndPlanIdIn(
            StatsPeriod period,
            List<Long> planIds
    );
}
