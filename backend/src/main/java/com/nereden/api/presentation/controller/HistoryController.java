package com.nereden.api.presentation.controller;

import com.nereden.api.application.dto.ApiResponse;
import com.nereden.api.application.dto.history.SearchHistoryResponse;
import com.nereden.api.application.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ApiResponse<List<SearchHistoryResponse>> list(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(historyService.getHistory(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveTextSearch(
            @AuthenticationPrincipal UUID userId,
            @RequestBody Map<String, String> body
    ) {
        historyService.saveTextSearch(userId, body.get("query"));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal UUID userId) {
        historyService.clearHistory(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        historyService.deleteItem(userId, id);
    }
}
