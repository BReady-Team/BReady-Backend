package com.bready.server.stats.dto;

import com.bready.server.stats.domain.StatsPeriod;
import lombok.Builder;

@Builder
public record StatsSummaryResponse(
        StatsPeriod period,
        long totalPlans,
        long totalSwitches,
        double avgSwitchesPerPlan,
        long recentCount
) {
}
