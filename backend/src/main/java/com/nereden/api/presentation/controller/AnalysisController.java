package com.nereden.api.presentation.controller;

import com.nereden.api.application.dto.ApiResponse;
import com.nereden.api.application.dto.analysis.AnalysisRequestResponse;
import com.nereden.api.application.dto.analysis.AnalysisResultResponse;
import com.nereden.api.application.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AnalysisRequestResponse> create(
            @AuthenticationPrincipal UUID userId,
            @RequestBody Map<String, String> body
    ) {
        final String imageUrl = body.get("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl gereklidir.");
        }
        return ApiResponse.of(analysisService.createRequest(userId, imageUrl));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnalysisRequestResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(analysisService.getRequest(id));
    }

    @GetMapping("/{id}/result")
    public ApiResponse<AnalysisResultResponse> getResult(@PathVariable UUID id) {
        return ApiResponse.of(analysisService.getResult(id));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<AnalysisRequestResponse> retry(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        return ApiResponse.of(analysisService.retry(id, userId));
    }
}
