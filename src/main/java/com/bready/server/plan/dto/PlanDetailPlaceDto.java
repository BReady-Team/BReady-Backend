package com.bready.server.plan.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PlanDetailPlaceDto {

    private Long id;
    private String externalId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isIndoor;

}
