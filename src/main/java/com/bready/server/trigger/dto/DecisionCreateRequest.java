package com.bready.server.trigger.dto;

import com.bready.server.trigger.domain.DecisionType;
import jakarta.validation.constraints.NotNull;

public record DecisionCreateRequest(
        @NotNull DecisionType decisionType
) {
}
