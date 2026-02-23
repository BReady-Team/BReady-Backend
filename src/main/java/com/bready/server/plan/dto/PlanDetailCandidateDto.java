package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanDetailCandidateDto {

    private Long candidateId;
    private PlanDetailPlaceDto place;
    private boolean isRepresentative;

}
