package com.nereden.api.application.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String role;
    @JsonProperty("isPremium")
    private boolean isPremium;
    private String createdAt;
    private String updatedAt;
}
