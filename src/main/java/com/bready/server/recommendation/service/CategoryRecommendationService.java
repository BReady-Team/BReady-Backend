package com.bready.server.recommendation.service;

import com.bready.server.recommendation.dto.CategoryRecommendationRequest;
import com.bready.server.recommendation.dto.CategoryRecommendationResponse;
import com.bready.server.recommendation.port.CategoryRecommendationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryRecommendationService {

    private final CategoryRecommendationPort categoryRecommendationPort;
    private final RecommendValidationService validationService;

    @Transactional(readOnly = true)
    public CategoryRecommendationResponse recommendCategories(
            Long userId, CategoryRecommendationRequest request
    ) {

        RecommendValidationService.Validated v = validationService.validateUserAndLoad(userId, request);

        List<CategoryRecommendationResponse.CategoryItem> items = categoryRecommendationPort.recommendCategories(
                v.planCategories(), v.currentCategory(), v.trigger().getTriggerType()
        );

        return new CategoryRecommendationResponse(items);
    }
}
