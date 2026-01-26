package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.repository.PlaceCandidateRepository;
import com.bready.server.plan.domain.CategoryState;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.plan.repository.CategoryStateRepository;
import com.bready.server.stats.service.PlanStatsService;
import com.bready.server.trigger.domain.Decision;
import com.bready.server.trigger.domain.SwitchLog;
import com.bready.server.trigger.dto.DecisionSwitchRequest;
import com.bready.server.trigger.dto.DecisionSwitchResponse;
import com.bready.server.trigger.exception.TriggerSwitchErrorCase;
import com.bready.server.trigger.repository.DecisionRepository;
import com.bready.server.trigger.repository.SwitchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SwitchService {

    private final DecisionRepository decisionRepository;
    private final SwitchLogRepository switchLogRepository;
    private final CategoryStateRepository categoryStateRepository;
    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlanStatsService planStatsService;

    @Transactional
    public DecisionSwitchResponse executeSwitch(Long decisionId, DecisionSwitchRequest request) {

        Decision decision = decisionRepository.findByIdWithTriggerPlanCategory(decisionId)
                .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.DECISION_NOT_FOUND));

        // SWITCH 결정인지 검증
        if (!decision.isSwitch()) {
            throw ApplicationException.from(TriggerSwitchErrorCase.KEEP_DECISION_CANNOT_SWITCH);
        }

        // 이미 전환된 decision인지 체크
        if (switchLogRepository.existsByDecision_Id(decisionId)) {
            throw ApplicationException.from(TriggerSwitchErrorCase.ALREADY_SWITCHED);
        }

        PlanCategory category = decision.getTrigger().getCategory();
        Long categoryId = category.getId();
        Long planId = decision.getTrigger().getPlan().getId();

        Long toCandidateId = request.toCandidateId();

        // 전환 대상 후보가 해당 카테고리 후보인지 검증 (soft delete 포함)
        if (!placeCandidateRepository.existsAliveByIdAndCategoryId(toCandidateId, categoryId)) {
            boolean existsAlive = placeCandidateRepository.findAliveById(toCandidateId).isPresent();
            if (!existsAlive) {
                throw ApplicationException.from(TriggerSwitchErrorCase.TO_CANDIDATE_NOT_FOUND);
            }
            throw ApplicationException.from(TriggerSwitchErrorCase.TO_CANDIDATE_MISMATCH);
        }

        // CategoryState 락 조회
        CategoryState state = categoryStateRepository.findByCategory_IdForUpdate(categoryId)
                .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.CATEGORY_STATE_NOT_FOUND));

        Long fromCandidateId = state.getCurrentCandidateId();

        // 동일 후보 전환 방지
        if (fromCandidateId != null && fromCandidateId.equals(toCandidateId)) {
            throw ApplicationException.from(TriggerSwitchErrorCase.SAME_CANDIDATE);
        }

        state.changeRepresentative(toCandidateId);

        SwitchLog saved;

        try {
            saved = switchLogRepository.saveAndFlush(
                    SwitchLog.create(
                            decision,
                            placeCandidateRepository.findAliveById(fromCandidateId)
                                    .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.CATEGORY_STATE_NOT_FOUND)),
                            placeCandidateRepository.findAliveById(toCandidateId)
                                    .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.TO_CANDIDATE_NOT_FOUND))
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.from(TriggerSwitchErrorCase.ALREADY_SWITCHED);
        }

        // 통계 업데이트
        planStatsService.increaseSwitchCount(planId);

        return DecisionSwitchResponse.builder()
                .switchLogId(saved.getId())
                .fromCandidateId(fromCandidateId)
                .toCandidateId(toCandidateId)
                .switchedAt(saved.getCreatedAt())
                .build();
    }
}