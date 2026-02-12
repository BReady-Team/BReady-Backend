package com.bready.server.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PlanCategoryOrderUpdateRequest {

    @NotEmpty(message = "orders는 비어있을 수 없습니다.")
    @Valid
    private List<OrderItem> orders;

    @Getter
    public static class OrderItem {

        @NotNull(message = "planCategoryId는 필수입니다.")
        private Long planCategoryId;

        @NotNull(message = "sequence는 필수입니다.")
        @Min(value = 1, message = "sequence는 1 이상이어야 합니다.")
        private Integer sequence;

    }
}
