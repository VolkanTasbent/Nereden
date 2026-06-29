package com.nereden.api.application.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private PriceRangeResponse priceRange;
    private List<StoreSummaryResponse> stores;
    private Double similarityScore;
    private boolean exactMatch;
    private String brand;
    private String createdAt;
}
