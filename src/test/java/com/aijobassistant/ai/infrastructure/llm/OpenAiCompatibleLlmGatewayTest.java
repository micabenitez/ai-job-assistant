package com.aijobassistant.ai.infrastructure.llm;

import com.aijobassistant.ai.shared.exception.LlmServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatibleLlmGatewayTest {

    @Test
    @DisplayName("Debe lanzar LlmServiceException cuando la URL no es accesible o la conexión es rechazada")
    void shouldThrowLlmServiceExceptionWhenServerIsUnreachable() {
        OpenAiCompatibleLlmGateway gateway = new OpenAiCompatibleLlmGateway(
                "http://127.0.0.1:59999",
                "fake-key",
                "fake-model",
                1
        );

        LlmServiceException ex = assertThrows(
                LlmServiceException.class,
                () -> gateway.generateStructuredResponse("system prompt", "user prompt")
        );

        assertTrue(ex.getMessage().contains("Falla en la comunicación con el servicio de IA"));
    }
}