package com.bready.server.trigger.dto;

import com.bready.server.trigger.domain.TriggerType;
import jakarta.validation.constraints.NotNull;

public record TriggerCreateRequest(
        @NotNull Long planId,
        @NotNull Long categoryId,
        @NotNull TriggerType triggerType
) {
}
