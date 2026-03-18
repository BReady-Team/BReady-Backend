package com.bready.server.plan.controller;

import com.bready.server.global.auth.CurrentUser;
import com.bready.server.global.exception.ApplicationException;
import com.bready.server.global.exception.GlobalExceptionHandler;
import com.bready.server.plan.dto.PlanCreateResponse;
import com.bready.server.plan.dto.PlanDeleteResponse;
import com.bready.server.plan.dto.PlanDetailResponse;
import com.bready.server.plan.dto.PlanDto;
import com.bready.server.plan.dto.PlanListItemDto;
import com.bready.server.plan.dto.PlanListResponse;
import com.bready.server.plan.dto.PlanUpdateResponse;
import com.bready.server.plan.exception.PlanErrorCase;
import com.bready.server.plan.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlanService planService;

    @InjectMocks
    private PlanController planController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(planController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentUser.class)
                                && parameter.getParameterType().equals(Long.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return 1L;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("플랜 생성 성공 → 201")
    void createPlan_success() throws Exception {
        String requestJson = """
                {
                  "title": "서울 여행",
                  "planDate": "2026-03-20",
                  "region": "서울"
                }
                """;

        PlanCreateResponse response = PlanCreateResponse.builder()
                .planId(10L)
                .createdAt(LocalDateTime.now())
                .build();

        given(planService.createPlan(anyLong(), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.planId").value(10L));
    }

    @Test
    @DisplayName("플랜 생성 validation 실패 → 400")
    void createPlan_validation_fail() throws Exception {
        mockMvc.perform(post("/api/v1/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플랜 수정 성공 → 200")
    void updatePlan_success() throws Exception {
        String requestJson = """
                {
                  "title": "부산 여행",
                  "planDate": "2026-03-18",
                  "region": "부산"
                }
                """;

        PlanUpdateResponse response = PlanUpdateResponse.builder()
                .planId(10L)
                .updatedAt(LocalDateTime.now())
                .build();

        given(planService.updatePlan(anyLong(), anyLong(), any()))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/plans/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(10L));
    }

    @Test
    @DisplayName("플랜 수정 - 없음 → 404")
    void updatePlan_not_found() throws Exception {
        String requestJson = """
                {
                  "title": "test",
                  "planDate": "2026-03-20",
                  "region": "서울"
                }
                """;

        given(planService.updatePlan(anyLong(), anyLong(), any()))
                .willThrow(ApplicationException.from(PlanErrorCase.PLAN_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/plans/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(PlanErrorCase.PLAN_NOT_FOUND.getErrorCode()));
    }

    @Test
    @DisplayName("플랜 수정 - 권한 없음 → 403")
    void updatePlan_access_denied() throws Exception {
        String requestJson = """
                {
                  "title": "test",
                  "planDate": "2026-03-20",
                  "region": "서울"
                }
                """;

        given(planService.updatePlan(anyLong(), anyLong(), any()))
                .willThrow(ApplicationException.from(PlanErrorCase.PLAN_ACCESS_DENIED));

        mockMvc.perform(patch("/api/v1/plans/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(PlanErrorCase.PLAN_ACCESS_DENIED.getErrorCode()));
    }

    @Test
    @DisplayName("플랜 상세 조회 성공 → 200")
    void getPlanDetail_success() throws Exception {
        PlanDto planDto = PlanDto.builder()
                .planId(10L)
                .title("서울 여행")
                .build();

        PlanDetailResponse response = PlanDetailResponse.builder()
                .plan(planDto)
                .categories(List.of())
                .build();

        given(planService.getPlanDetail(anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/plans/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plan.planId").value(10L))
                .andExpect(jsonPath("$.data.plan.title").value("서울 여행"));
    }

    @Test
    @DisplayName("플랜 상세 조회 - 없음 → 404")
    void getPlanDetail_not_found() throws Exception {
        given(planService.getPlanDetail(anyLong(), anyLong()))
                .willThrow(ApplicationException.from(PlanErrorCase.PLAN_NOT_FOUND));

        mockMvc.perform(get("/api/v1/plans/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(PlanErrorCase.PLAN_NOT_FOUND.getErrorCode()));
    }

    @Test
    @DisplayName("내 플랜 목록 조회 → 200")
    void getMyPlans_success() throws Exception {
        PlanListItemDto item = PlanListItemDto.builder()
                .planId(10L)
                .title("서울 여행")
                .build();

        PlanListResponse response = PlanListResponse.builder()
                .items(List.of(item))
                .pageInfo(null)
                .build();

        given(planService.getMyPlans(anyLong(), anyInt(), anyInt(), any()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].planId").value(10L))
                .andExpect(jsonPath("$.data.items[0].title").value("서울 여행"));
    }

    @Test
    @DisplayName("플랜 삭제 성공 → 200")
    void deletePlan_success() throws Exception {
        PlanDeleteResponse response = PlanDeleteResponse.builder()
                .planId(10L)
                .build();

        given(planService.deletePlan(anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(delete("/api/v1/plans/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(10L));
    }

    @Test
    @DisplayName("플랜 삭제 - 권한 없음 → 403")
    void deletePlan_access_denied() throws Exception {
        given(planService.deletePlan(anyLong(), anyLong()))
                .willThrow(ApplicationException.from(PlanErrorCase.PLAN_ACCESS_DENIED));

        mockMvc.perform(delete("/api/v1/plans/10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(PlanErrorCase.PLAN_ACCESS_DENIED.getErrorCode()));
    }
}