package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.dto.PlanActivitiesResponse;
import com.bready.server.stats.service.PlanActivityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
@Validated
public class PlanActivityStatsController {

    private final PlanActivityStatsService planActivityStatsService;

    @GetMapping("/plans/{planId}/activities")
    @Operation(
            summary = "플랜 단일 활동 로그 조회",
            description = "특정 플랜에서 발생한 최근 전환/결정 활동 로그를 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "플랜 ID 값이 잘못됨",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "limit 값 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanActivitiesResponse> getPlanActivities(
            @CurrentUser Long userId,
            @PathVariable @Positive Long planId,
            @Parameter(description = "최근 N개 (기본 5, 최대 20)", example = "5")
            @RequestParam(required = false) Integer limit
    ) {
        return CommonResponse.success(planActivityStatsService.getPlanActivities(userId, planId, limit));
    }
}
