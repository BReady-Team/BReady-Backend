package com.bready.server.trigger.controller;

import com.bready.server.global.response.CommonResponse;
import com.bready.server.trigger.dto.DecisionSwitchRequest;
import com.bready.server.trigger.dto.DecisionSwitchResponse;
import com.bready.server.trigger.service.SwitchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/triggers")
@RequiredArgsConstructor
@Validated
public class SwitchController {

    private final SwitchService switchService;

    @PostMapping("/{decisionId}/switch")
    @Operation(
            summary = "장소 전환 확정",
            description = "SWITCH 결정에 대해서만 실제 대표 장소 후보를 변경하고 SwitchLog를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전환 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "KEEP 결정 전환 시도 / 잘못된 후보",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "결정/후보/상태 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 전환 완료",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    public CommonResponse<DecisionSwitchResponse> executeSwitch(
            @PathVariable @Positive Long decisionId,
            @RequestBody @Valid DecisionSwitchRequest request
    ) {
        return CommonResponse.success(
                switchService.executeSwitch(decisionId, request)
        );
    }
}
