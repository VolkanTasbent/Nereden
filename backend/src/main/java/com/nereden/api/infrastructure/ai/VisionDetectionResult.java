package com.nereden.api.infrastructure.ai;

import com.nereden.api.domain.entity.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public record VisionDetectionResult(
        String title,
        String description,
        String brand,
        ProductCategory category,
        BigDecimal estimatedMinPrice,
        BigDecimal estimatedMaxPrice,
        double confidence,
        List<String> searchKeywords,
        List<String> similarAlternatives
) {
    public VisionDetectionResult {
        searchKeywords = searchKeywords == null ? List.of() : searchKeywords;
        similarAlternatives = similarAlternatives == null ? List.of() : similarAlternatives;
    }
}
