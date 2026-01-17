package com.bready.server.stats.repository;

import com.bready.server.stats.domain.PlanStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanStatsRepository extends JpaRepository<PlanStats, Long> {
}
