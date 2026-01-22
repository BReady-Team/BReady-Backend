package com.bready.server.place.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude,

        Boolean isIndoor
) {}