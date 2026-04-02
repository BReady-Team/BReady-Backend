package com.bready.server.recommendation.service;

import com.bready.server.recommendation.dto.CategoryRecommendationRequest;
import com.bready.server.recommendation.dto.CategoryRecommendationResponse;
import com.bready.server.recommendation.port.CategoryRecommendationPort;
import com.bready.server.recommendation.cache.CategoryRecommendationCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryRecommendationService {

    private final CategoryRecommendationPort categoryRecommendationPort;
    private final RecommendValidationService validationService;
    private final CategoryRecommendationCacheService categoryRecommendationCacheService;

    @Transactional(readOnly = true)
    public CategoryRecommendationResponse recommendCategories(
            Long userId, CategoryRecommendationRequest request
    ) {

        long start = System.currentTimeMillis();

        RecommendValidationService.Validated v = validationService.validateUserAndLoad(userId, request);
        long t1 = System.currentTimeMillis();

        String cacheKey = categoryRecommendationCacheService.generateKey(request.triggerId());

        List<CategoryRecommendationResponse.CategoryItem> cachedItems = categoryRecommendationCacheService.get(cacheKey);

        if (cachedItems != null && !cachedItems.isEmpty()) {
            return new CategoryRecommendationResponse(cachedItems);
        }

        List<CategoryRecommendationResponse.CategoryItem> items = categoryRecommendationPort.recommendCategories(
                v.planCategories(), v.currentCategory(), v.trigger().getTriggerType()
        );
        long t2 = System.currentTimeMillis();

        log.info("categoryReco validate={}ms, recommend={}ms, total={}ms",
                t1 - start, t2 - t1, t2 - start);

        categoryRecommendationCacheService.put(cacheKey, items);

        return new CategoryRecommendationResponse(items);
    }
}
