package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PlanCategoryTypeUpdateRequest {
    @NotNull
    private PlaceCategoryType categoryType;
}