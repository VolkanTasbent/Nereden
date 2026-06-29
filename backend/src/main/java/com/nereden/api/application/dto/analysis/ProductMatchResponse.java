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
public class ProductMatchResponse {
    private ProductSummaryResponse exactMatch;
    private List<ProductSummaryResponse> similarProducts;
    private List<ProductSummaryResponse> cheaperAlternatives;
    private PriceRangeResponse estimatedPriceRange;
}
