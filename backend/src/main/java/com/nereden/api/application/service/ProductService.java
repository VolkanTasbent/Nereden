package com.nereden.api.application.service;

import com.nereden.api.application.dto.ApiMeta;
import com.nereden.api.application.dto.analysis.ProductSummaryResponse;
import com.nereden.api.application.dto.product.ProductDetailResponse;
import com.nereden.api.application.mapper.ProductMapper;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.ProductCategory;
import com.nereden.api.domain.repository.ProductRepository;
import com.nereden.api.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public ProductDetailResponse getProduct(UUID productId) {
        final Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı."));
        return ProductMapper.toDetail(product, storeRepository.findByProductId(productId));
    }

    public FeedResult getFeed(int page, int limit, String category) {
        final PageRequest pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        final Page<Product> result = category != null && !category.isBlank()
                ? productRepository.findByCategory(parseCategory(category), pageable)
                : productRepository.findAll(pageable);

        final List<ProductSummaryResponse> items = result.getContent().stream()
                .map(p -> ProductMapper.toSummary(p, storeRepository.findByProductId(p.getId()), false, null))
                .toList();

        return new FeedResult(items, ApiMeta.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .build());
    }

    public FeedResult search(String query, int page, int limit, String category) {
        if (query == null || query.isBlank()) {
            return getFeed(page, limit, category);
        }

        final PageRequest pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        final Page<Product> result = category != null && !category.isBlank()
                ? productRepository.searchByCategory(query.trim(), parseCategory(category), pageable)
                : productRepository.search(query.trim(), pageable);

        final List<ProductSummaryResponse> items = result.getContent().stream()
                .map(p -> ProductMapper.toSummary(p, storeRepository.findByProductId(p.getId()), false, null))
                .toList();

        return new FeedResult(items, ApiMeta.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .build());
    }

    private ProductCategory parseCategory(String category) {
        try {
            return ProductCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Geçersiz kategori: " + category);
        }
    }

    public record FeedResult(List<ProductSummaryResponse> items, ApiMeta meta) {}
}
