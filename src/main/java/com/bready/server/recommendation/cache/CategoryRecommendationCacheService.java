package com.bready.server.recommendation.cache;

import com.bready.server.recommendation.dto.CategoryRecommendationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryRecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    @Value("${recommendation.ai.cache.category-ttl-seconds:300}")
    private long categoryCacheTtlSeconds;

    // 캐시 키 생성
    // category 추천은 triggerId 기준으로 묶음
    public String generateKey(Long triggerId) {
        return "reco:category:%d".formatted(triggerId);
    }

    // 캐시 조회
    public List<CategoryRecommendationResponse.CategoryItem> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return objectMapper.convertValue(
                value,
                new TypeReference<List<CategoryRecommendationResponse.CategoryItem>>() {}
        );
    }

    // 캐시 저장
    public void put(String key, List<CategoryRecommendationResponse.CategoryItem> items) {
        redisTemplate.opsForValue().set(
                key,
                items,
                Duration.ofSeconds(categoryCacheTtlSeconds)
        );
    }
}
