package com.bready.server.stats.dto;

import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.domain.TriggerType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlanActivityItem(
        String activityId,
        TriggerType triggerType,
        DecisionType decisionType,
        LocalDateTime createdAt
) {
}
