package com.nereden.api.infrastructure.ai;

public interface VisionAiClient {
    VisionDetectionResult analyze(String imageUrl);
    boolean isAvailable();
}
