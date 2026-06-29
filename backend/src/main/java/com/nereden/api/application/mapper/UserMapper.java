package com.nereden.api.application.mapper;

import com.nereden.api.application.dto.auth.AuthResponse;
import com.nereden.api.application.dto.auth.AuthTokensResponse;
import com.nereden.api.application.dto.auth.UserResponse;
import com.nereden.api.domain.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name().toLowerCase())
                .isPremium(user.isPremium())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .build();
    }

    public static AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .user(toResponse(user))
                .tokens(AuthTokensResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }
}
