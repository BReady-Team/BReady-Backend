package com.bready.server.recommendation.dto;


import jakarta.validation.constraints.NotNull;

public record PlaceRecommendationRequest(
        @NotNull Long triggerId
) {}
