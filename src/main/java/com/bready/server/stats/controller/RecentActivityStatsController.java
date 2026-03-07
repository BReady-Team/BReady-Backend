package com.bready.server.stats.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.stats.domain.StatsPeriod;
import com.bready.server.stats.dto.RecentActivitiesResponse;
import com.bready.server.stats.service.RecentActivityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
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
public class RecentActivityStatsController {

    private final RecentActivityStatsService recentActivityStatsService;

    @GetMapping("/activities")
    @Operation(
            summary = "전체 최근 활동 로그 조회",
            description = "현재 로그인 사용자의 전체 플랜 기준 최근 활동 로그를 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "period 값이 잘못됨",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "limit 값 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<RecentActivitiesResponse> getRecentActivities(
            @CurrentUser Long userId,
            @Parameter(description = "조회 기간 (WEEK | MONTH | ALL)", example = "ALL")
            @RequestParam StatsPeriod period,
            @Parameter(description = "최근 N개 (기본 3, 최대 20)", example = "3")
            @RequestParam(required = false) @Positive @Max(20) Integer limit
    ) {
        return CommonResponse.success(recentActivityStatsService.getRecentActivities(userId, period, limit));
    }
}
