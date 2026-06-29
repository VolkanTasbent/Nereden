package com.nereden.api.application.mapper;

import com.nereden.api.application.dto.analysis.PriceRangeResponse;
import com.nereden.api.application.dto.analysis.ProductSummaryResponse;
import com.nereden.api.application.dto.analysis.StoreSummaryResponse;
import com.nereden.api.application.dto.product.ProductDetailResponse;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.Store;

import java.util.Comparator;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductSummaryResponse toSummary(Product product, List<Store> stores, boolean exactMatch, Double similarity) {
        return ProductSummaryResponse.builder()
                .id(product.getId().toString())
                .title(product.getTitle())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory().name().toLowerCase())
                .priceRange(toPriceRange(product))
                .stores(sortStoresByPrice(stores).stream().map(ProductMapper::toStoreSummary).toList())
                .similarityScore(similarity)
                .exactMatch(exactMatch)
                .brand(product.getBrand())
                .createdAt(product.getCreatedAt().toString())
                .build();
    }

    public static ProductDetailResponse toDetail(Product product, List<Store> stores) {
        return ProductDetailResponse.builder()
                .id(product.getId().toString())
                .title(product.getTitle())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory().name().toLowerCase())
                .priceRange(toPriceRange(product))
                .stores(sortStoresByPrice(stores).stream().map(ProductMapper::toStoreSummary).toList())
                .exactMatch(false)
                .brand(product.getBrand())
                .createdAt(product.getCreatedAt().toString())
                .build();
    }

    public static PriceRangeResponse toPriceRange(Product product) {
        if (product.getMinPrice() == null || product.getMaxPrice() == null) {
            return null;
        }
        return PriceRangeResponse.builder()
                .min(product.getMinPrice())
                .max(product.getMaxPrice())
                .currency(product.getCurrency())
                .build();
    }

    private static List<Store> sortStoresByPrice(List<Store> stores) {
        return stores.stream()
                .sorted(Comparator
                        .comparing(Store::getPrice, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Store::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static StoreSummaryResponse toStoreSummary(Store store) {
        return StoreSummaryResponse.builder()
                .id(store.getId().toString())
                .name(store.getName())
                .url(store.getUrl())
                .logoUrl(store.getLogoUrl())
                .price(store.getPrice())
                .currency(store.getCurrency())
                .inStock(store.isInStock())
                .build();
    }
}
