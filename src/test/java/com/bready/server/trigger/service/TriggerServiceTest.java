package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.plan.repository.PlanCategoryRepository;
import com.bready.server.stats.service.PlanStatsService;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.domain.TriggerType;
import com.bready.server.trigger.dto.TriggerCreateRequest;
import com.bready.server.trigger.dto.TriggerCreateResponse;
import com.bready.server.trigger.exception.TriggerErrorCase;
import com.bready.server.trigger.repository.TriggerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TriggerServiceTest {

    @InjectMocks
    private TriggerService triggerService;

    @Mock private PlanCategoryRepository planCategoryRepository;
    @Mock private TriggerRepository triggerRepository;
    @Mock private PlanStatsService planStatsService;
    @Mock private CategoryStateRepository categoryStateRepository;

    @Test
    @DisplayName("트리거 생성 성공")
    void createTrigger_success() {
        Long planId = 1L;
        Long categoryId = 1L;
        Long currentCandidateId = 10L;

        PlanCategory category = mock(PlanCategory.class);
        Plan plan = mock(Plan.class);
        CategoryState state = mock(CategoryState.class);
        Trigger trigger = mock(Trigger.class);

        given(planCategoryRepository.findByIdAndPlan_Id(categoryId, planId))
                .willReturn(Optional.of(category));

        given(category.getPlan()).willReturn(plan);
        given(plan.getId()).willReturn(planId);

        given(category.getId()).willReturn(categoryId);

        given(categoryStateRepository.findByCategory_Id(categoryId))
                .willReturn(Optional.of(state));

        given(state.getCurrentCandidateId()).willReturn(currentCandidateId);

        given(triggerRepository.save(any())).willReturn(trigger);

        given(trigger.getId()).willReturn(100L);
        given(trigger.getOccurredAt()).willReturn(LocalDateTime.now());

        TriggerCreateResponse response =
                triggerService.createTrigger(new TriggerCreateRequest(planId, categoryId, TriggerType.WEATHER_BAD));

        assertThat(response.triggerId()).isEqualTo(100L);
        verify(planStatsService).increaseTriggerCount(planId);
    }

    @Test
    @DisplayName("트리거 생성 실패 - CategoryState 없음")
    void createTrigger_categoryStateNotFound() {
        PlanCategory category = mock(PlanCategory.class);

        given(planCategoryRepository.findByIdAndPlan_Id(1L, 1L))
                .willReturn(Optional.of(category));

        given(categoryStateRepository.findByCategory_Id(any()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                triggerService.createTrigger(new TriggerCreateRequest(1L, 1L, TriggerType.FATIGUE))
        )
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException ae = (ApplicationException) ex;
                    assertThat(ae.getErrorCase()).isEqualTo(TriggerErrorCase.CATEGORY_STATE_NOT_FOUND);
                });
    }
}