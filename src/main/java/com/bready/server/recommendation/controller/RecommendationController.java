package com.bready.server.recommendation.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.recommendation.dto.*;
import com.bready.server.recommendation.service.CategoryRecommendationService;
import com.bready.server.recommendation.service.PlaceRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@Validated
@RequiredArgsConstructor
public class RecommendationController {

    private final PlaceRecommendationService recommendationService;
    private final CategoryRecommendationService categoryRecommendationService;

    @PostMapping("/places")
    @Operation(
            summary = "전환 장소 추천",
            description = "트리거 발생 이후, 특정 플랜/카테고리 기준으로 대체 장소 추천"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "플랜 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜/카테고리/트리거/추천결과 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "502", description = "외부 장소 검색 실패 (API 장애/요청 오류)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<PlaceRecommendationResponse> recommendPlaces(
            @CurrentUser Long userId,
            @ParameterObject @ModelAttribute PlaceRecommendationQuery query,
            @RequestBody @Valid PlaceRecommendationRequest request
    ) {
        return CommonResponse.success(recommendationService.recommendPlaces(userId, request, query));
    }

    @PostMapping("/categories")
    @Operation(
            summary = "전환 카테고리 추천",
            description = "트리거 발생 이후, 플랜 전체 흐름을 고려한 카테고리 대체 추천"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "플랜 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "플랜/카테고리/트리거 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<CategoryRecommendationResponse> recommendCategories(
            @CurrentUser Long userId,
            @RequestBody @Valid CategoryRecommendationRequest request
    ) {
        return CommonResponse.success(categoryRecommendationService.recommendCategories(userId, request));
    }
}
