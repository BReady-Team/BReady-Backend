package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanCategoryItemDto {

    private Long planCategoryId;
    private PlaceCategoryType categoryType;
    private Integer sequence;

}
