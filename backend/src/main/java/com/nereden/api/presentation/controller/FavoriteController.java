package com.nereden.api.presentation.controller;

import com.nereden.api.application.dto.ApiResponse;
import com.nereden.api.application.dto.favorite.FavoriteResponse;
import com.nereden.api.application.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(favoriteService.getFavorites(userId));
    }

    @GetMapping("/{productId}/status")
    public ApiResponse<Boolean> status(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID productId
    ) {
        return ApiResponse.of(favoriteService.isFavorite(userId, productId));
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FavoriteResponse> add(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID productId
    ) {
        return ApiResponse.of(favoriteService.addFavorite(userId, productId));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID productId
    ) {
        favoriteService.removeFavorite(userId, productId);
    }
}
