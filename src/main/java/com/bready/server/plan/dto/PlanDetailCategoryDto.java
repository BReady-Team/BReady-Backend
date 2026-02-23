package com.bready.server.plan.dto;

import com.bready.server.place.domain.PlaceCategoryType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlanDetailCategoryDto {

    private Long planCategoryId;
    private PlaceCategoryType categoryType;
    private Integer sequence;

    private Long representativeCandidateId;

    @Builder.Default
    private List<PlanDetailCandidateDto> candidates = List.of();
}
