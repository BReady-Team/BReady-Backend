package com.bready.server.recommendation.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record PlaceRecommendationRequest(
        @NotNull Long planId,
        @NotNull Long categoryId,
        @NotNull Long triggerId) {}
