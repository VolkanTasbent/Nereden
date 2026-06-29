package com.nereden.api.application.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequestResponse {
    private String id;
    private String userId;
    private String imageUrl;
    private String status;
    private String createdAt;
    private String completedAt;
    private String errorMessage;
}
