package com.bready.server.recommendation.adapter;

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

    private final RuleBasedCategoryRecommendationAdapter rulBasedAdapter;
    private final AiRerankService aiRerankService;

    @Override
    public List<CategoryRecommendationResponse.CategoryItem> recommendCategories(List<PlanCategory> planCategories, PlanCategory currentCategory, TriggerType triggerType) {
        List<CategoryRecommendationResponse.CategoryItem> base = rulBasedAdapter.recommendCategories(planCategories, currentCategory, triggerType);

        if (base.isEmpty()) return base;

        List<CategoryRerankCandidate> targets = base.stream()
                .map(i -> new CategoryRerankCandidate(i.categoryType().name(), i.label()))
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

        return "trigger=" + triggerType
                + ", current=" + currentCategory.getCategoryType().name()
                + ", planFlow=[" + flow + "]";
    }

    private record CategoryRerankCandidate(String id, String name) implements AiRerankTarget {}
}
