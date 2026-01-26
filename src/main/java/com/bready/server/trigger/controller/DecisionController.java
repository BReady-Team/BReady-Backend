package com.bready.server.trigger.controller;

import com.bready.server.global.response.CommonResponse;
import com.bready.server.trigger.dto.DecisionCreateRequest;
import com.bready.server.trigger.dto.DecisionCreateResponse;
import com.bready.server.trigger.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/triggers")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping("/{triggerId}/decision")
    @Operation(
            summary = "트리거 결정 등록",
            description = "발생한 트리거에 대해 유지(KEEP) 또는 전환(SWITCH)을 결정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 트리거",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 결정된 트리거",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<DecisionCreateResponse> createDecision(
            @PathVariable Long triggerId,
            @RequestBody @Valid DecisionCreateRequest request
    ) {
        return CommonResponse.success(
                decisionService.createDecision(triggerId, request)
        );
    }
}
