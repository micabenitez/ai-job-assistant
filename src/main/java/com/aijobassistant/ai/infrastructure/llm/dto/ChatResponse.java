package com.aijobassistant.ai.infrastructure.llm.dto;

import java.util.List;

public record ChatResponse(List<Choice> choices) {
    public record Choice(ChatMessage message) {
    }
}