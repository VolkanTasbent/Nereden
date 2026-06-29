package com.nereden.api.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Primary
public class VisionAiService {

    private final List<VisionAiClient> clients;

    public VisionAiService(List<VisionAiClient> clients) {
        this.clients = clients;
    }

    public VisionDetectionResult analyze(String imageUrl) {
        for (VisionAiClient client : clients) {
            if (client instanceof GeminiVisionClient && client.isAvailable()) {
                try {
                    log.info("Using Gemini vision for analysis");
                    return client.analyze(imageUrl);
                } catch (Exception ex) {
                    log.warn("Gemini vision failed, falling back to next client", ex);
                }
            }
        }

        for (VisionAiClient client : clients) {
            if (client instanceof OpenAiVisionClient && client.isAvailable()) {
                try {
                    log.info("Using OpenAI vision for analysis");
                    return client.analyze(imageUrl);
                } catch (Exception ex) {
                    log.warn("OpenAI vision failed, falling back to next client", ex);
                }
            }
        }

        return clients.stream()
                .filter(c -> !(c instanceof GeminiVisionClient))
                .filter(c -> !(c instanceof OpenAiVisionClient))
                .filter(VisionAiClient::isAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Vision AI client not available"))
                .analyze(imageUrl);
    }
}
