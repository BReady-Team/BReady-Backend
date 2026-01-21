package com.bready.server.place.controller;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.global.response.CommonResponse;
import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.place.dto.PlaceSearchResponse;
import com.bready.server.place.exception.PlaceErrorCase;
import com.bready.server.place.service.PlaceSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Validated
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    @Operation(
            summary = "장소 검색",
            description =
                    """
                    카카오 장소 검색 API를 사용하여 사용자가 선택한 BReady 카테고리 기준으로 주변 장소 목록을 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (카테고리 누락, 반경 범위 초과 등)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "검색 결과 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<List<PlaceSearchResponse>> searchPlaces(
            @Parameter(
                    name = "category",
                    description = "BReady 장소 카테고리",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "CAFE"
            )
            @RequestParam PlaceCategoryType category,

            @Parameter(
                    name = "latitude",
                    description = "사용자 위도",
                    in = ParameterIn.QUERY,
                    example = "37.544"
            )
            @RequestParam(required = false) Double latitude,

            @Parameter(
                    name = "longitude",
                    description = "사용자 경도",
                    in = ParameterIn.QUERY,
                    example = "127.055"
            )
            @RequestParam(required = false) Double longitude,

            @Parameter(
                    name = "radius",
                    description = "검색 반경 (미터 단위, 최대 10,000m)",
                    in = ParameterIn.QUERY,
                    example = "2000"
            )
            @RequestParam(defaultValue = "2000")
            @Min(0) @Max(10000) Integer radius
    ) {
        // 좌표는 위도,경도 둘다 필요
        if ((latitude == null && longitude != null) ||
                (latitude != null && longitude == null)) {
            throw ApplicationException.from(PlaceErrorCase.LOCATION_REQUIRED);
        }

        List<PlaceSearchResponse> results =
                placeSearchService.search(category, latitude, longitude, radius);

        // 결과 없으면 에러 처리
        if (results.isEmpty()) {
            throw ApplicationException.from(PlaceErrorCase.PLACE_NOT_FOUND);
        }

        return CommonResponse.success(results);
    }
}