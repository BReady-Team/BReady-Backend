package com.bready.server.recommendation.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlaceRecommendationRequest(
        @NotNull @Positive Long triggerId
) {}
