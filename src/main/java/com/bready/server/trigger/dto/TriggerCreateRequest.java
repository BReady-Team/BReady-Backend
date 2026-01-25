package com.bready.server.trigger.dto;

import com.bready.server.trigger.domain.TriggerType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TriggerCreateRequest(
        @NotNull @Positive Long planId,
        @NotNull @Positive Long categoryId,
        @NotNull TriggerType triggerType
) {
}
