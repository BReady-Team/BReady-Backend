package com.bready.server.trigger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DecisionSwitchRequest(
        @NotNull @Positive Long toCandidateId
) {}