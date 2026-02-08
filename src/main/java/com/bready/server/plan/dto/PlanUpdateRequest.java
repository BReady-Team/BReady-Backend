package com.bready.server.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PlanUpdateRequest {

    @NotBlank(message = "플랜 제목을 입력해주세요.")
    private String title;

    @NotNull(message = "플랜 날짜를 선택해주세요.")
    private LocalDate planDate;

    @NotBlank(message = "지역 정보를 입력해주세요.")
    private String region;

}
