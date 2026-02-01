package com.bready.server.plan.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.plan.dto.PlanCreateRequest;
import com.bready.server.plan.dto.PlanCreateResponse;
import com.bready.server.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "플랜 생성",
            description = "인증된 사용자가 새로운 플랜을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "플랜 생성 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (누락/형식오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "플랜 생성 실패 (서버 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanCreateResponse> createPlan(
            @CurrentUser Long userId,
            @Valid @RequestBody PlanCreateRequest request
    ) {
        return CommonResponse.success(planService.createPlan(userId, request));
    }
}
