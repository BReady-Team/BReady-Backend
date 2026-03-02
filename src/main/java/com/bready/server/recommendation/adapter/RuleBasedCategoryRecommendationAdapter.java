package com.bready.server.recommendation.adapter;

import com.bready.server.place.domain.PlaceCategoryType;
import com.bready.server.plan.domain.PlanCategory;
import com.bready.server.recommendation.dto.CategoryRecommendationResponse;
import com.bready.server.recommendation.port.CategoryRecommendationPort;
import com.bready.server.trigger.domain.TriggerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleBasedCategoryRecommendationAdapter implements CategoryRecommendationPort {

    @Override
    public List<CategoryRecommendationResponse.CategoryItem> recommendCategories(List<PlanCategory> planCategories, PlanCategory currentCategory, TriggerType triggerType) {

        List<CategoryRecommendationResponse.CategoryItem> result = new ArrayList<>();
        PlaceCategoryType currentType = currentCategory.getCategoryType();

        // 화이트리스트
        for (PlaceCategoryType type : PlaceCategoryType.values()) {

            if (type == currentType) continue;

            if (triggerType == TriggerType.FATIGUE && type == PlaceCategoryType.WALK)  continue;

            if (triggerType == TriggerType.WEATHER_BAD && !type.isIndoor()) continue;

            result.add(new CategoryRecommendationResponse.CategoryItem(
                    type,
                    type.getLabel(),
                    buildReason(triggerType)
            ));
        }
        
        return result;
    }

    private String buildReason(TriggerType triggerType) {
        return switch (triggerType) {
            case WEATHER_BAD -> "날씨 악화로 인해 실내 활동이 적합합니다.";
            case WAITING_TOO_LONG -> "혼잡을 피할 수 있는 대체 활동입니다.";
            case PLACE_CLOSED -> "영업 종료 상황을 고려한 대안입니다.";
            case FATIGUE -> "체력 부담이 적은 활동을 추천합니다.";
            case DISTANCE_TOO_FAR -> "이동 부담을 줄일 수 있는 대안입니다.";
        };
    }
}
