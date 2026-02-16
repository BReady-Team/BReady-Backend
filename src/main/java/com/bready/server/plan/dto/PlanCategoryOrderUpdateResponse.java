package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanCategoryOrderUpdateResponse {

    private Long planId;
    private LocalDateTime updatedAt;

}
