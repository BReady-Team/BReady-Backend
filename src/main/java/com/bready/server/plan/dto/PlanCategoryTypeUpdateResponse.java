package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanCategoryTypeUpdateResponse {
    private Long planId;
    private Long planCategoryId;
    private PlaceCategoryType categoryType;
    private Integer sequence;
    private boolean resetCandidates;
    private LocalDateTime updatedAt;
}
