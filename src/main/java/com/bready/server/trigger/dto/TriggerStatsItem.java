package com.bready.server.trigger.dto;

import com.bready.server.trigger.domain.TriggerType;
import lombok.Builder;

@Builder
public record TriggerStatsItem(
        TriggerType triggerType,
        long count,
        int percentage
) {}
