package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PlanDetailResponse {

    private PlanDto plan;
    // TODO : 장소, 카테고리 등 연결

}
