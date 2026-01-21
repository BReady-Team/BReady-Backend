package com.bready.server.place.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PlaceSearchResponse(
        String externalId,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean isIndoor
) {}