package com.bready.server.recommendation.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(ChatClient.class)
public class OpenAiRerankService implements AiRerankService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public AiRerankResult rerank(String context, List<? extends AiRerankTarget> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return new AiRerankResult(List.of(), Map.of());
        }

        try {
            String candidatesJson = objectMapper.writeValueAsString(
                    candidates.stream()
                            .map(AiRerankTarget::toPromptAttributes)
                            .toList()
            );

            String prompt = """
                    후보 리스트를 재정렬하고 후보별 이유를 1문장으로 작성한다.
                                        절대 새 후보를 만들지 마라. id는 주어진 후보 id만 사용한다.
                                        반드시 JSON만 출력하라.
                    
                                        [context]
                                        %s
                    
                                        [candidates]
                                        %s
                    
                                        [output schema]
                                        {
                                          "rankedIds": ["id1","id2"],
                                          "reasonsById": { "id1": "이유 1문장" }
                                        }
                    """.formatted(
                    context == null ? "" : context,
                    candidatesJson
            );

            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            AiRerankResult parsed = objectMapper.readValue(content, AiRerankResult.class);

            if (parsed == null || parsed.rankedIds() == null || parsed.rankedIds().isEmpty()) {
                return new AiRerankResult(List.of(), Map.of());
            }

            return parsed;
        } catch (Exception e) {
            return new AiRerankResult(List.of(), Map.of());
        }
    }
}
