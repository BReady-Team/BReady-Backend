package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanCategoryDeleteResponse {

    private Long planId;
    private Long planCategoryId;
    private LocalDateTime deletedAt;

}
