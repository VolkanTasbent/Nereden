package com.nereden.api.application.dto.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryResponse {
    private String id;
    private String query;
    private String imageUrl;
    private String productId;
    private String productTitle;
    private String createdAt;
}
