package com.bready.server.trigger.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record DecisionCreateResponse(
        Long decisionId,
        String decisionType,
        LocalDateTime decidedAt,
        Boolean needSwitch // SWITCH일 때만 true
) {
}
