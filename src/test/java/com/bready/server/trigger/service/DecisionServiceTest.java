package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.trigger.domain.Decision;
import com.bready.server.trigger.domain.DecisionType;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.DecisionCreateRequest;
import com.bready.server.trigger.dto.DecisionCreateResponse;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.TriggerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @InjectMocks
    private DecisionService decisionService;

    @Mock
    private TriggerRepository triggerRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @Test
    @DisplayName("결정 생성 성공 - SWITCH")
    void createDecision_success_switch() {
        Long triggerId = 1L;
        Trigger trigger = mock(Trigger.class);

        Decision decision = mock(Decision.class);
        given(decision.getId()).willReturn(10L);
        given(decision.getDecisionType()).willReturn(DecisionType.SWITCH);
        given(decision.getDecidedAt()).willReturn(LocalDateTime.now());
        given(decision.isSwitch()).willReturn(true);

        given(triggerRepository.findById(triggerId)).willReturn(Optional.of(trigger));

        given(decisionRepository.save(any())).willReturn(decision);

        DecisionCreateResponse response =
                decisionService.createDecision(triggerId, new DecisionCreateRequest(DecisionType.SWITCH));

        assertThat(response.decisionId()).isEqualTo(10L);
        assertThat(response.decisionType()).isEqualTo("SWITCH");
        assertThat(response.needSwitch()).isTrue();
    }

    @Test
    @DisplayName("결정 생성 실패 - 트리거 없음")
    void createDecision_triggerNotFound() {
        given(triggerRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> decisionService.createDecision(1L, new DecisionCreateRequest(DecisionType.KEEP))
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("존재하지 않는 트리거");
    }

    @Test
    @DisplayName("결정 생성 실패 - 이미 결정됨")
    void createDecision_alreadyDecided() {
        Trigger trigger = mock(Trigger.class);

        given(triggerRepository.findById(1L)).willReturn(Optional.of(trigger));

        given(decisionRepository.save(any())).willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> decisionService.createDecision(1L, new DecisionCreateRequest(DecisionType.KEEP))
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("이미 결정이 완료된");
    }
}
