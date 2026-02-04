package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.TriggerStatsResponse;
import com.bready.server.stats.service.TriggerStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class TriggerStatsController {

    private final TriggerStatsService triggerStatsService;

    @GetMapping("/triggers")
    @Operation(
            summary = "트리거 분석 통계 조회",
            description = "사용자의 플랜 전반에서 발생한 트리거 유형별 발생 빈도를 집계합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "period 값이 잘못됨")
    })
    public CommonResponse<TriggerStatsResponse> getTriggerStats(
            @CurrentUser Long userId,
            @Parameter(description = "조회 기간 (WEEK | MONTH | ALL)", example = "ALL")
            @RequestParam StatsPeriod period
    ) {
        return CommonResponse.success(
                triggerStatsService.getTriggerStats(userId, period)
        );
    }
}
