package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.StatsSummaryResponse;
import com.bready.server.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    @Operation(
            summary = "통계 요약 조회",
            description = "사용자의 전체 플랜 데이터를 기반으로 통계 페이지 상단 요약 카드 지표를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "period 값이 잘못됨",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<StatsSummaryResponse> getSummary(
            @CurrentUser Long userId,
            @Parameter(description = "조회 기간 (WEEK | MONTH | ALL)", example = "ALL")
            @RequestParam StatsPeriod period
    ) {
        return CommonResponse.success(
                statsService.getSummary(userId, period)
        );
    }
}
