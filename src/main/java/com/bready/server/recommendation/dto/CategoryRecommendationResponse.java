package com.bready.server.recommendation.dto;

import com.bready.server.place.domain.PlaceCategoryType;

import java.util.List;

public record CategoryRecommendationResponse(List<CategoryItem> items) {
    public record CategoryItem(PlaceCategoryType categoryType, String label, String reason) {}
}
