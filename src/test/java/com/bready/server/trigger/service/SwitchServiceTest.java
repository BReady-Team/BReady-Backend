package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCandidate;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.Plan;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.stats.service.PlanStatsService;
import com.bready.server.trigger.domain.Decision;
import com.bready.server.trigger.domain.SwitchLog;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.DecisionSwitchRequest;
import com.bready.server.trigger.dto.DecisionSwitchResponse;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.SwitchLogRepository;
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
class SwitchServiceTest {

    @InjectMocks
    private SwitchService switchService;

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private SwitchLogRepository switchLogRepository;
    @Mock
    private CategoryStateRepository categoryStateRepository;
    @Mock
    private PlaceCandidateRepository placeCandidateRepository;
    @Mock
    private PlanStatsService planStatsService;

    @Test
    @DisplayName("SWITCH 결정 전환 성공")
    void executeSwitch_success() {
        Long decisionId = 1L;
        Long categoryId = 2L;
        Long planId = 3L;
        Long fromCandidateId = 10L;
        Long toCandidateId = 20L;

        Decision decision = mock(Decision.class);
        Trigger trigger = mock(Trigger.class);
        Plan plan = mock(Plan.class);
        PlanCategory category = mock(PlanCategory.class);
        CategoryState state = mock(CategoryState.class);

        PlaceCandidate fromCandidate = mock(PlaceCandidate.class);
        PlaceCandidate toCandidate = mock(PlaceCandidate.class);
        SwitchLog switchLog = mock(SwitchLog.class);

        given(decision.isSwitch()).willReturn(true);
        given(decision.getTrigger()).willReturn(trigger);

        given(trigger.getCategory()).willReturn(category);
        given(category.getId()).willReturn(categoryId);

        given(trigger.getPlan()).willReturn(plan);
        given(plan.getId()).willReturn(planId);

        given(decisionRepository.findByIdWithTriggerPlanCategory(decisionId))
                .willReturn(Optional.of(decision));

        given(switchLogRepository.existsByDecision_Id(decisionId))
                .willReturn(false);

        given(placeCandidateRepository.findAliveByIdAndCategoryId(toCandidateId, categoryId))
                .willReturn(Optional.of(toCandidate));

        given(categoryStateRepository.findByCategory_IdForUpdate(categoryId))
                .willReturn(Optional.of(state));

        given(state.getCurrentCandidateId())
                .willReturn(fromCandidateId);

        given(placeCandidateRepository.findAliveById(fromCandidateId))
                .willReturn(Optional.of(fromCandidate));

        given(switchLogRepository.saveAndFlush(any()))
                .willReturn(switchLog);

        given(switchLog.getId()).willReturn(100L);
        given(switchLog.getCreatedAt()).willReturn(LocalDateTime.now());

        DecisionSwitchResponse response = switchService.executeSwitch(decisionId, new DecisionSwitchRequest(toCandidateId));


        assertThat(response.fromCandidateId()).isEqualTo(fromCandidateId);
        assertThat(response.toCandidateId()).isEqualTo(toCandidateId);
        verify(planStatsService).increaseSwitchCount(planId);
    }

    @Test
    @DisplayName("전환 실패 - KEEP 결정")
    void executeSwitch_keepDecision() {
        Decision decision = mock(Decision.class);
        given(decision.isSwitch()).willReturn(false);

        given(decisionRepository.findByIdWithTriggerPlanCategory(1L))
                .willReturn(Optional.of(decision));

        assertThatThrownBy(() -> switchService.executeSwitch(1L, new DecisionSwitchRequest(20L))
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("KEEP 결정");
    }

    @Test
    @DisplayName("전환 실패 - 이미 전환됨")
    void executeSwitch_alreadySwitched() {
        Decision decision = mock(Decision.class);
        given(decision.isSwitch()).willReturn(true);

        given(decisionRepository.findByIdWithTriggerPlanCategory(1L))
                .willReturn(Optional.of(decision));

        given(switchLogRepository.existsByDecision_Id(1L))
                .willReturn(true);

        assertThatThrownBy(() -> switchService.executeSwitch(1L, new DecisionSwitchRequest(20L))
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("이미 전환");
    }
}
