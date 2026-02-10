package com.bready.server.plan.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.plan.dto.PlanCategoryCreateRequest;
import com.bready.server.plan.dto.PlanCategoryCreateResponse;
import com.bready.server.plan.dto.PlanCategoryDeleteResponse;
import com.bready.server.plan.service.PlanCategoryService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans/{planId}/categories")
public class PlanCategoryController {

    private final PlanCategoryService planCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "카테고리 추가",
            description = "플랜에 카테고리를 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 추가 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (누락/형식오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "카테고리 추가 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "카테고리 추가 실패 (서버 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanCategoryCreateResponse> addCategory(
            @CurrentUser Long userId,
            @PathVariable Long planId,
            @Valid @RequestBody PlanCategoryCreateRequest request
    ) {
        return CommonResponse.success(planCategoryService.addCategory(userId, planId, request));
    }


    @DeleteMapping("/{planCategoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "카테고리 삭제",
            description = "인증된 사용자가 플랜에 속한 카테고리 삭제 (soft delete)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "카테고리 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜 또는 카테고리 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "카테고리 삭제 실패 (서버 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlanCategoryDeleteResponse> deleteCategory(
            @CurrentUser Long userId,
            @PathVariable Long planId,
            @PathVariable Long planCategoryId
    ) {
        return CommonResponse.success(planCategoryService.deleteCategory(userId, planId, planCategoryId));
    }
}
