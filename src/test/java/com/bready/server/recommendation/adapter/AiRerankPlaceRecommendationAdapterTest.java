package com.bready.server.recommendation.adapter;

import com.bready.server.recommendation.ai.AiRerankResult;
import com.bready.server.recommendation.ai.AiRerankService;
import com.bready.server.recommendation.dto.PlaceRecommendationResponse;
import com.bready.server.trigger.domain.TriggerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AiRerankPlaceRecommendationAdapterTest {

    @InjectMocks
    private AiRerankPlaceRecommendationAdapter adapter;

    @Mock
    private RuleBasedRecommendationAdapter ruleBasedAdapter;

    @Mock
    private AiRerankService aiRerankService;

    @Test
    @DisplayName("AI rerank 적용 성공 - 순서/이유가 AI 결과로 반영")
    void recommendPlaceCandidates_success_applyAiRerank() {

        // given
        List<PlaceRecommendationResponse.RecommendationItem> base = List.of(
                new PlaceRecommendationResponse.RecommendationItem("id1", "장소1", "주소1", BigDecimal.ONE, BigDecimal.ONE, true, 100, "rule-이유1"),
                new PlaceRecommendationResponse.RecommendationItem("id2", "장소2", "주소2", BigDecimal.TEN, BigDecimal.TEN, true, 100, "rule-이유2")
        );

        given(ruleBasedAdapter.recommendPlaceCandidates(any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .willReturn(base);

        given(aiRerankService.rerank(anyString(), anyList()))
                .willReturn(new AiRerankResult(
                        List.of("id2", "id1"),
                        Map.of(
                                "id2", "ai-이유2",
                                "id1", "ai-이유1"
                        )
                ));

        // when
        List<PlaceRecommendationResponse.RecommendationItem> result =
                adapter.recommendPlaceCandidates(null, TriggerType.WEATHER_BAD, "서울", 50.0, 50.0, 2000, 10, "id-original");

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).externalId()).isEqualTo("id2");
        assertThat(result.get(0).reason()).isEqualTo("ai-이유2");
        assertThat(result.get(1).externalId()).isEqualTo("id1");
        assertThat(result.get(1).reason()).isEqualTo("ai-이유1");

        verify(ruleBasedAdapter).recommendPlaceCandidates(any(), any(), any(), any(), any(), anyInt(), anyInt(), any());
        verify(aiRerankService).rerank(anyString(), anyList());
    }

}
