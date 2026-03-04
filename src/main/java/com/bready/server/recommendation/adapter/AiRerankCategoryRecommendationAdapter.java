package com.bready.server.recommendation.adapter;

import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.ai.AiRerankResult;
import com.bready.server.recommendation.ai.AiRerankService;
import com.bready.server.recommendation.ai.AiRerankTarget;
import com.bready.server.recommendation.dto.CategoryRecommendationResponse;
import com.bready.server.recommendation.port.CategoryRecommendationPort;
import com.bready.server.trigger.domain.TriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "recommendation.ai", name = "enabled", havingValue = "true")
public class AiRerankCategoryRecommendationAdapter implements CategoryRecommendationPort {

    private final RuleBasedCategoryRecommendationAdapter ruleBasedAdapter;
    private final AiRerankService aiRerankService;

    @Override
    public List<CategoryRecommendationResponse.CategoryItem> recommendCategories(List<PlanCategory> planCategories, PlanCategory currentCategory, TriggerType triggerType) {
        List<CategoryRecommendationResponse.CategoryItem> base = ruleBasedAdapter.recommendCategories(planCategories, currentCategory, triggerType);

        if (base.isEmpty()) return base;

        List<CategoryRerankCandidate> targets = base.stream()
                .map(i -> new CategoryRerankCandidate(i.categoryType().name(), i.label(), i.categoryType()))
                .toList();

        String context = buildContext(triggerType, currentCategory, planCategories);

        AiRerankResult result = aiRerankService.rerank(context, targets);
        if (result == null || result.rankedIds() == null || result.rankedIds().isEmpty()) {
            return base;
        }

        // base 기준으로 재정렬, 중복 제거
        Map<String, CategoryRecommendationResponse.CategoryItem> itemMap = new LinkedHashMap<>();
        for (CategoryRecommendationResponse.CategoryItem item : base) {
            itemMap.putIfAbsent(item.categoryType().name(), item);
        }

        Set<String> added = new HashSet<>();
        List<CategoryRecommendationResponse.CategoryItem> reordered = new ArrayList<>();

        for (String id : result.rankedIds()) {
            CategoryRecommendationResponse.CategoryItem item = itemMap.get(id);
            if (item == null) continue;

            if (!added.add(id)) continue;

            String reason = (result.reasonsById() != null)
                    ? result.reasonsById().getOrDefault(id, item.reason())
                    : item.reason();

            reordered.add(new CategoryRecommendationResponse.CategoryItem(
                    item.categoryType(),
                    item.label(),
                    reason
            ));
        }

        for (CategoryRecommendationResponse.CategoryItem item : base) {
            String id = item.categoryType().name();
            if (!added.contains(id)) {
                reordered.add(item);
            }
        }

        return reordered;
    }

    private String buildContext(TriggerType triggerType, PlanCategory currentCategory, List<PlanCategory> planCategories) {
        String flow = planCategories.stream()
                .sorted(Comparator.comparing(PlanCategory::getSequence))
                .map(c -> c.getSequence() + ":" + c.getCategoryType().name())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return """
        당신은 플랜 전환 카테고리 추천 시스템이다.
        반드시 candidates에 제공된 id만 사용해야 한다.
        새로운 후보를 생성하면 안 된다.

        추천 기준
        - 트리거 해결에 도움이 되는 활동
        - 현재 카테고리와 성격이 다른 활동
        - 플랜 흐름의 다양성 유지

        trigger=%s
        current=%s
        planFlow=[%s]
        """.formatted(
                triggerType,
                currentCategory.getCategoryType().name(),
                flow
        );
    }

    private record CategoryRerankCandidate(String id, String name, PlaceCategoryType type) implements AiRerankTarget {
        @Override
        public Map<String, Object> toPromptAttributes() {
            return Map.of(
                    "id", id,
                    "name", name,
                    "indoor", type.isIndoor(),
                    "keyword", type.getKeyword()
            );
        }
    }

}
