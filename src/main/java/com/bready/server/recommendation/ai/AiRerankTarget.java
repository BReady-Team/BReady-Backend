package com.bready.server.recommendation.ai;

import java.util.Map;

public interface AiRerankTarget {

    String getId();
    String getName();

    default Map<String, Object> toPromptAttributes() {
        return Map.of(
                "id", getId(),
                "name", getName()
        );
    }
}
