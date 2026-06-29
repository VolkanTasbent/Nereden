package com.nereden.api.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class GeminiApiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final List<String> flashModels;
    private final List<String> proModels;

    public GeminiApiService(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-2.5-flash}") String model,
            @Value("${app.ai.gemini.pro-model:gemini-2.5-pro}") String proModel,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.flashModels = buildCandidates(model, "gemini-2.5-flash", "gemini-flash-latest");
        this.proModels = buildCandidates(proModel, "gemini-2.5-pro", "gemini-pro-latest");
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<String> flashModels() {
        return flashModels;
    }

    public List<String> proModels() {
        return proModels;
    }

    public String generateJsonWithImage(
            String prompt,
            VisionImageLoader.LoadedImage image,
            List<String> modelCandidates,
            double temperature
    ) {
        final List<Object> parts = new ArrayList<>();
        parts.add(Map.of("text", prompt));
        parts.add(Map.of("inline_data", Map.of(
                "mime_type", image.mimeType(),
                "data", image.base64Data()
        )));

        return generateJson(modelCandidates, temperature, parts);
    }

    public String generateJsonText(String prompt, List<String> modelCandidates, double temperature) {
        return generateJson(modelCandidates, temperature, List.of(Map.of("text", prompt)));
    }

    public JsonNode generateWithGoogleSearch(String prompt, List<String> modelCandidates, double temperature) {
        Exception lastError = null;

        for (String model : modelCandidates) {
            try {
                log.debug("Calling Gemini with Google Search: {}", model);
                final Map<String, Object> body = Map.of(
                        "contents", List.of(Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )),
                        "tools", List.of(Map.of("google_search", Map.of())),
                        "generationConfig", Map.of("temperature", temperature)
                );

                final String response = restClient.post()
                        .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                return objectMapper.readTree(response);
            } catch (Exception ex) {
                lastError = ex;
                if (shouldTryNextModel(ex)) {
                    log.warn("Gemini search model {} unavailable, trying next", model);
                    continue;
                }
                break;
            }
        }

        throw new IllegalStateException("Gemini arama isteği başarısız oldu.", lastError);
    }

    private String generateJson(
            List<String> modelCandidates,
            double temperature,
            List<Object> parts
    ) {
        Exception lastError = null;

        for (String model : modelCandidates) {
            try {
                log.debug("Calling Gemini model: {}", model);
                final Map<String, Object> body = Map.of(
                        "contents", List.of(Map.of("role", "user", "parts", parts)),
                        "generationConfig", Map.of(
                                "responseMimeType", "application/json",
                                "temperature", temperature
                        )
                );

                final String response = restClient.post()
                        .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                return extractText(response);
            } catch (Exception ex) {
                lastError = ex;
                if (shouldTryNextModel(ex)) {
                    log.warn("Gemini model {} unavailable, trying next", model);
                    continue;
                }
                break;
            }
        }

        throw new IllegalStateException("Gemini isteği başarısız oldu.", lastError);
    }

    private String extractText(String response) throws Exception {
        final JsonNode root = objectMapper.readTree(response);
        final String content = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Gemini boş yanıt döndü.");
        }
        return content;
    }

    private boolean shouldTryNextModel(Exception ex) {
        if (ex instanceof RestClientResponseException restEx) {
            final int status = restEx.getStatusCode().value();
            final String body = restEx.getResponseBodyAsString();
            return status == 429
                    || status == 404
                    || body.contains("RESOURCE_EXHAUSTED")
                    || body.contains("not found");
        }
        return false;
    }

    private List<String> buildCandidates(String primary, String... fallbacks) {
        final Set<String> models = new LinkedHashSet<>();
        if (primary != null && !primary.isBlank()) {
            models.add(primary);
        }
        for (String fallback : fallbacks) {
            models.add(fallback);
        }
        return new ArrayList<>(models);
    }
}
