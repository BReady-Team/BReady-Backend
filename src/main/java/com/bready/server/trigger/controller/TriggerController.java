package com.bready.server.trigger.controller;

import com.bready.server.global.response.CommonResponse;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.service.TriggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/triggers")
@RequiredArgsConstructor
public class TriggerController {

    private final TriggerService triggerService;

    @PostMapping
    @Operation(
            summary = "트리거 발생",
            description = "사용자가 특정 플랜/카테고리에서 상황 발생을 선언합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "트리거 발생 성공",
                    content = @Content(
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 플랜 또는 카테고리",
                    content = @Content(
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "트리거 발생 권한 없음 (플랜 소유자 아님)",
                    content = @Content(
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            )
    })
    public CommonResponse<TriggerCreateResponse> createTrigger(
            @RequestBody @Valid TriggerCreateRequest request
    ) {
        return CommonResponse.success(
                triggerService.createTrigger(request)
        );
    }
}