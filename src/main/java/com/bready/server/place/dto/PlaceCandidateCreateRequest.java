package com.bready.server.place.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceCandidateCreateRequest(
        @NotNull
        Long planId,
        @NotNull
        Long categoryId,

        @NotBlank
        String externalId,
        @NotBlank
        String name,

        String address,

        @NotNull
        BigDecimal latitude,
        @NotNull
        BigDecimal longitude,

        Boolean isIndoor
) {}