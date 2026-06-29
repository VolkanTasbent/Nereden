package com.nereden.api.infrastructure.ai;

import com.nereden.api.domain.entity.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public record ProductIdentification(
        String title,
        String description,
        String brand,
        ProductCategory category,
        double confidence,
        String color,
        String material,
        String productType
) {}
