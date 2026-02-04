package com.bready.server.stats.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PlanActivitiesResponse(
        Long planId,
        String planTitle,
        int limit,
        List<PlanActivityItem> items
) {
}