package com.nereden.api.application.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultResponse {
    private String id;
    private String requestId;
    private ProductMatchResponse matches;
    private double confidence;
    private long processingTimeMs;
    private String createdAt;
}
