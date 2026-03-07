package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.PlanStatsResponse;
import com.bready.server.stats.service.PlanStatsMaterializedService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
@Validated
public class PlanStatsController {
    private final PlanStatsMaterializedService materializedService;

    @GetMapping("/plans")
    public CommonResponse<PlanStatsResponse> getPlanStats(
            @CurrentUser Long ownerId,
            @RequestParam @NotNull StatsPeriod period,
            @RequestParam(required = false) @Positive @Max(50) Integer limit
    ) {
        return CommonResponse.success(materializedService.getStats(ownerId, period, limit));
    }
}