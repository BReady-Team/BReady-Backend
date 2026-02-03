package com.bready.server.trigger.dto;

import com.bready.server.stats.domain.StatsPeriod;
import lombok.Builder;

import java.util.List;

@Builder
public record TriggerStatsResponse(
        StatsPeriod period,
        long totalCount,
        List<TriggerStatsItem> items
) {}
