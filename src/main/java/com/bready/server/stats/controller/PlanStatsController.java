package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.PlanStatsResponse;
import com.bready.server.stats.service.PlanStatsJoinService;
import com.bready.server.stats.service.PlanStatsMaterializedService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotNull;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
@Validated
public class PlanStatsController {

    private final PlanStatsJoinService joinService;
    private final PlanStatsMaterializedService materializedService;

    /*
    @GetMapping("/plans")
    @Operation(
            summary = "플랜별 통계 목록 조회",
            description = "현재 로그인 사용자가 생성한 모든 플랜을 대상으로, 플랜별 전환 횟수 및 기본 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "요청 파라미터가 올바르지 않습니다.",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            )
    })
    public CommonResponse<PlanStatsResponse> getPlanStats(
            @Parameter(hidden = true)
            @CurrentUser Long ownerId,
            @Parameter(description = "조회 기간 (WEEK | MONTH | ALL)", example = "ALL")
            @RequestParam @NotNull StatsPeriod period,
            @Parameter(description = "목록 개수 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) @Positive @Max(50) Integer limit
    ) {
        return CommonResponse.success(planStatsJoinService.getPlanStats(ownerId, period, limit));
    }
     */

    // JOIN 기반 조회
    @GetMapping("/plans/join")
    public CommonResponse<PlanStatsResponse> getJoinStats(
            @CurrentUser Long ownerId,
            @RequestParam @NotNull StatsPeriod period,
            @RequestParam(required = false) @Positive @Max(50) Integer limit
    ) {

        return CommonResponse.success(
                joinService.getStats(ownerId, period, limit)
        );
    }

    // Materialized 조회
    @GetMapping("/plans/materialized")
    public CommonResponse<PlanStatsResponse> getMaterializedStats(
            @CurrentUser Long ownerId,
            @RequestParam @NotNull StatsPeriod period,
            @RequestParam(required = false) @Positive @Max(50) Integer limit
    ) {

        return CommonResponse.success(
                materializedService.getStats(ownerId, period, limit)
        );
    }
}