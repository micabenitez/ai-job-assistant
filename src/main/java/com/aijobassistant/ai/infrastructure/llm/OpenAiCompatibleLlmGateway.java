package com.aijobassistant.ai.infrastructure.llm;

import com.aijobassistant.ai.infrastructure.llm.dto.ChatRequest;
import com.aijobassistant.ai.infrastructure.llm.dto.ChatResponse;
import com.aijobassistant.ai.shared.domain.LlmGateway;
import com.aijobassistant.ai.shared.exception.LlmServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class OpenAiCompatibleLlmGateway implements LlmGateway {

    private final RestClient restClient;
    private final String model;

    public OpenAiCompatibleLlmGateway(
            @Value("${llm.api.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${llm.api.key:dummy-key}") String apiKey,
            @Value("${llm.api.model:gpt-4o-mini}") String model,
            @Value("${llm.api.timeout-seconds:45}") int timeoutSeconds) {

        this.model = model;

        // Configuración de timeouts de red mediante factory nativo
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateStructuredResponse(String systemPrompt, String userPrompt) {
        ChatRequest requestPayload = ChatRequest.ofJsonFormat(
                this.model,
                systemPrompt,
                userPrompt,
                0.1 // Baja temperatura para resultados determinísticos y menor variabilidad
        );

        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestPayload)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new LlmServiceException("El proveedor de IA devolvió una respuesta vacía o sin alternativas.");
            }

            return response.choices().getFirst().message().content();

        } catch (LlmServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmServiceException("Falla en la comunicación con el servicio de IA: " + e.getMessage(), e);
        }
    }
}