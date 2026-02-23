package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PlanDetailResponse {

    private PlanDto plan;

    @Builder.Default
    private List<PlanDetailCategoryDto> categories = List.of();

}
