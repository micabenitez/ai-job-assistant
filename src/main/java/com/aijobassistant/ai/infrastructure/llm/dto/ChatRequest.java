package com.aijobassistant.ai.infrastructure.llm.dto;

import java.util.List;
import java.util.Map;

public record ChatRequest(
        String model,
        List<ChatMessage> messages,
        double temperature,
        Map<String, String> response_format
) {
    public static ChatRequest ofJsonFormat(String model, String systemPrompt, String userPrompt, double temperature) {
        return new ChatRequest(
                model,
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ),
                temperature,
                Map.of("type", "json_object")
        );
    }
}