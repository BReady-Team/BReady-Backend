package com.bready.server.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceRecommendationAiService {

    private final ChatClient chatClient;

    public String ping() {
        return chatClient.prompt()
                .user("ping")
                .call()
                .content();
    }
}
