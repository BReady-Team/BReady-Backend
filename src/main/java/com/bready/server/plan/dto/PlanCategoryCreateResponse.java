package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanCategoryCreateResponse {

    private Long planCategoryId;
    private Long planId;
    private PlaceCategoryType categoryType;
    private Integer sequence;
    private LocalDateTime createdAt;

}
