package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PlanCategoryCreateRequest {

    @NotNull(message = "categoryType은 필수입니다.")
    private PlaceCategoryType categoryType;

}
