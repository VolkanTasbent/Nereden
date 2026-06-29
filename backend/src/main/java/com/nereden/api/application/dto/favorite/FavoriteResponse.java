package com.nereden.api.application.dto.favorite;

import com.nereden.api.application.dto.analysis.ProductSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {
    private String id;
    private String productId;
    private ProductSummaryResponse product;
    private String createdAt;
}
