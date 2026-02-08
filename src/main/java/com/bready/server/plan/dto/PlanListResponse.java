package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlanListResponse {

    private List<PlanListItemDto> items;
    private PageInfo pageInfo;

}
