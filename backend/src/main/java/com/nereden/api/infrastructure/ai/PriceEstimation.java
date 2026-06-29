package com.nereden.api.infrastructure.ai;

import java.math.BigDecimal;
import java.util.List;

public record PriceEstimation(
        BigDecimal estimatedMinPrice,
        BigDecimal estimatedMaxPrice,
        List<String> searchKeywords,
        List<String> similarAlternatives,
        String priceReasoning
) {}
