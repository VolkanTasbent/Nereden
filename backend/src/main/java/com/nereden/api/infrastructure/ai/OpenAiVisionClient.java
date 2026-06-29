package com.nereden.api.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.openai.api-key", havingValue = ".+", matchIfMissing = false)
public class OpenAiVisionClient implements VisionAiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final VisionResponseParser responseParser;
    private final String model;
    private final boolean enabled;

    public OpenAiVisionClient(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model,
            ObjectMapper objectMapper,
            VisionResponseParser responseParser
    ) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.responseParser = responseParser;
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public VisionDetectionResult analyze(String imageUrl) {
        final Map<String, Object> body = Map.of(
                "model", model,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", VisionResponseParser.legacyCombinedPrompt()),
                                Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
                        )
                ))
        );

        try {
            final String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            final JsonNode root = objectMapper.readTree(response);
            final String content = root.path("choices").path(0).path("message").path("content").asText();
            return responseParser.parseLegacyCombined(content);
        } catch (Exception ex) {
            log.error("OpenAI vision analysis failed", ex);
            throw new IllegalStateException("AI analizi başarısız oldu.");
        }
    }
}
