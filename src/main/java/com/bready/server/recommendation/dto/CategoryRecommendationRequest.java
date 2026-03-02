package com.bready.server.recommendation.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryRecommendationRequest (
        @NotNull Long planId,
        @NotNull Long categoryId,
        @NotNull Long triggerId
){ }
