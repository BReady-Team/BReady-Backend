package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.trigger.domain.Decision;
import com.bready.server.trigger.domain.Trigger;
import com.bready.server.trigger.dto.DecisionCreateRequest;
import com.bready.server.trigger.dto.DecisionCreateResponse;
import com.bready.server.trigger.exception.TriggerDecisionErrorCase;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private final TriggerRepository triggerRepository;
    private final DecisionRepository decisionRepository;

    @Transactional
    public DecisionCreateResponse createDecision(
            Long triggerId,
            DecisionCreateRequest request
    ) {
        Trigger trigger = triggerRepository.findById(triggerId)
                .orElseThrow(() ->
                        ApplicationException.from(TriggerDecisionErrorCase.TRIGGER_NOT_FOUND)
                );

        // 이미 결정된 트리거인지 확인
        if (decisionRepository.existsByTrigger_Id(triggerId)) {
            throw ApplicationException.from(TriggerDecisionErrorCase.DECISION_ALREADY_MADE);
        }

        Decision decision = decisionRepository.save(
                Decision.create(trigger, request.decisionType())
        );


        return DecisionCreateResponse.builder()
                .decisionId(decision.getId())
                .decisionType(decision.getDecisionType().name())
                .decidedAt(decision.getDecidedAt())
                .needSwitch(decision.isSwitch() ? true : null)
                .build();
    }
}
