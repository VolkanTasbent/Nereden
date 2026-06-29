package com.nereden.api.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiVisionClient implements VisionAiClient {

    private final GeminiApiService geminiApiService;
    private final VisionImageLoader imageLoader;
    private final VisionResponseParser responseParser;

    @Override
    public boolean isAvailable() {
        return geminiApiService.isAvailable();
    }

    @Override
    public VisionDetectionResult analyze(String imageUrl) {
        final VisionImageLoader.LoadedImage image = imageLoader.load(imageUrl);

        log.info("AI: Single-pass product analysis");
        final VisionDetectionResult result = responseParser.parseLegacyCombined(
                geminiApiService.generateJsonWithImage(
                        VisionResponseParser.combinedAnalysisPrompt(),
                        image,
                        geminiApiService.flashModels(),
                        0.15
                )
        );

        log.info("AI completed — {} ({} - {} TL, confidence: {})",
                result.title(),
                result.estimatedMinPrice(),
                result.estimatedMaxPrice(),
                result.confidence());

        return result;
    }
}
