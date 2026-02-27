package com.bready.server.recommendation.ai;

import java.util.List;
import java.util.Map;

public record AiRerankResult (
        List<String> rankedIds,
        Map<String, String> reasonsById
) {}
