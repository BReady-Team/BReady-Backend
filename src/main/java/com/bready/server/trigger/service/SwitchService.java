package com.bready.server.trigger.service;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.domain.PlaceCandidate;
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

        // 전환 대상 후보 조회 + 검증 (중복 쿼리 제거)
        PlaceCandidate toCandidate =
                placeCandidateRepository.findAliveByIdAndCategoryId(toCandidateId, categoryId)
                        .orElseThrow(() -> {
                            boolean existsAlive =
                                    placeCandidateRepository.findAliveById(toCandidateId).isPresent();
                            return existsAlive
                                    ? ApplicationException.from(
                                            TriggerSwitchErrorCase.TO_CANDIDATE_MISMATCH
                                    )
                                    : ApplicationException.from(
                                            TriggerSwitchErrorCase.TO_CANDIDATE_NOT_FOUND
                                    );
                        });

        // CategoryState 락 걸고 조회
        CategoryState state = categoryStateRepository
                .findByCategory_IdForUpdate(categoryId)
                .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.CATEGORY_STATE_NOT_FOUND));

        Long fromCandidateId = state.getCurrentCandidateId();

        // 기존 대표 후보 없d으면 전환 불가
        if (fromCandidateId == null) {
            throw ApplicationException.from(TriggerSwitchErrorCase.FROM_CANDIDATE_NOT_FOUND);
        }

        // 동일 후보 전환 방지
        if (fromCandidateId.equals(toCandidateId)) {
            throw ApplicationException.from(TriggerSwitchErrorCase.SAME_CANDIDATE);
        }

        // 기존의 대표 후보 조회
        PlaceCandidate fromCandidate =
                placeCandidateRepository.findAliveById(fromCandidateId)
                        .orElseThrow(() -> ApplicationException.from(TriggerSwitchErrorCase.FROM_CANDIDATE_NOT_FOUND));

        state.changeRepresentative(toCandidateId);

        SwitchLog saved;
        try {
            saved = switchLogRepository.saveAndFlush(
                    SwitchLog.create(
                            decision,
                            fromCandidate,
                            toCandidate
                    )
            );
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약으로 인한 중복 전환 방지
            throw ApplicationException.from(TriggerSwitchErrorCase.ALREADY_SWITCHED);
        }

        planStatsService.increaseSwitchCount(planId);

        return DecisionSwitchResponse.builder()
                .switchLogId(saved.getId())
                .fromCandidateId(fromCandidateId)
                .toCandidateId(toCandidateId)
                .switchedAt(saved.getCreatedAt())
                .build();
    }
}