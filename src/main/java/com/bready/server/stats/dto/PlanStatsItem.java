package com.bready.server.stats.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlanStatsItem(
        Long planId,
        String planTitle,
        LocalDate planDate,
        String region,
        List<String> categoryTypes,
        Long totalSwitches
) {
}
