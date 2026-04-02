package com.bready.server.recommendation.cache;

import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${recommendation.ai.cache.place-ttl-seconds:180}")
    private long placeCacheTtlSeconds;

    // 캐시 키 생성
    // category + 반올림 latitude + 반올림 longitude
    public String generateKey(String categoryType, Double latitude, Double longitude) {
        return "reco:place:%s:%s:%s".formatted(
                categoryType,
                roundCoordinate(latitude),
                roundCoordinate(longitude)
        );
    }

    // 캐시 조회
    public List<PlaceRecommendationResponse.RecommendationItem> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return objectMapper.convertValue(
                value,
                new TypeReference<List<PlaceRecommendationResponse.RecommendationItem>>() {}
        );
    }

    // 캐시 저장
    public void put(String key, List<PlaceRecommendationResponse.RecommendationItem> items) {
        redisTemplate.opsForValue().set(
                key,
                items,
                Duration.ofSeconds(placeCacheTtlSeconds)
        );
    }

    // 좌표 반올림 헬퍼 메서드
    private String roundCoordinate(Double value) {
        if (value == null) {
            return "null";
        }

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
