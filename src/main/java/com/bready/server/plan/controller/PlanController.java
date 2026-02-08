package com.bready.server.plan.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.plan.dto.*;
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


    @PatchMapping("/{planId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "플랜 수정",
            description = "플랜의 제목, 날짜, 지역을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (누락/형식오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "플랜 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "플랜 수정 실패 (서버 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanUpdateResponse> updatePlan(
            @CurrentUser Long userId,
            @PathVariable Long planId,
            @Valid @RequestBody PlanUpdateRequest request
    ) {
        return CommonResponse.success(planService.updatePlan(userId, planId, request));
    }


    @GetMapping("/{planId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "플랜 상세 조회",
            description = "플랜 기본 정보 조회 (카테고리/장소 추후 확장)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "플랜 조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "플랜 조회 실패 (서버 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanDetailResponse> getPlanDetail(
            @CurrentUser Long userId,
            @PathVariable Long planId
    ) {
        return CommonResponse.success(planService.getPlanDetail(userId, planId));
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "내 플랜 목록 조회",
            description = "인증된 사용자의 플랜 목록 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
    })
    public CommonResponse<PlanListResponse> getMyPlans(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String order
    ) {
        return CommonResponse.success(planService.getMyPlans(userId, page, size, order));
    }


    @DeleteMapping("/{planId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "플랜 삭제",
            description = "인증된 사용자가 본인의 플랜 삭제 (soft delete)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플랜 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "플랜 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanDeleteResponse> deletePlan(@CurrentUser Long userId, @PathVariable Long planId) {
        return CommonResponse.success(planService.deletePlan(userId, planId));
    }

}
