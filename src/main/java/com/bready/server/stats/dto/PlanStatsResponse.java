package com.bready.server.stats.dto;

import com.bready.server.stats.domain.StatsPeriod;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanStatsResponse(
        StatsPeriod period,
        List<PlanStatsItem> items
) {
}
