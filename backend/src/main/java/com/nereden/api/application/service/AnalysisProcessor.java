package com.nereden.api.application.service;

import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.domain.entity.AnalysisRequest;
import com.nereden.api.domain.entity.AnalysisStatus;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.Store;
import com.nereden.api.domain.repository.AnalysisRequestRepository;
import com.nereden.api.domain.repository.ProductRepository;
import com.nereden.api.domain.repository.StoreRepository;
import com.nereden.api.infrastructure.ai.VisionAiService;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisProcessor {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final VisionAiService visionAiService;
    private final MarketSearchService marketSearchService;
    private final HistoryService historyService;

    @Async("analysisExecutor")
    @Transactional
    public void processAsync(UUID requestId) {
        final AnalysisRequest request = analysisRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return;
        }

        try {
            request.setStatus(AnalysisStatus.PROCESSING);
            analysisRequestRepository.save(request);

            final VisionDetectionResult detection = visionAiService.analyze(request.getImageUrl());
            final List<MarketOffer> marketOffers = marketSearchService.search(detection, request.getImageUrl());
            final VisionDetectionResult enriched = enrichWithMarketPrices(detection, marketOffers);

            request.setDetectedTitle(enriched.title());
            request.setConfidence(enriched.confidence());

            final Product exact = createProductFromDetection(request, enriched);
            createStoresForProduct(exact, marketOffers);

            findSimilarProducts(enriched, exact, request.getId());

            historyService.saveImageSearch(request.getUser().getId(), request.getImageUrl(), exact);

            request.setStatus(AnalysisStatus.COMPLETED);
            request.setCompletedAt(Instant.now());
            request.setErrorMessage(null);
            analysisRequestRepository.save(request);

            log.info("Analysis {} completed with title: {}", requestId, enriched.title());
        } catch (Throwable ex) {
            log.error("Analysis {} failed", requestId, ex);
            request.setStatus(AnalysisStatus.FAILED);
            request.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : "Analiz başarısız oldu.");
            request.setCompletedAt(Instant.now());
            analysisRequestRepository.save(request);
        }
    }

    private VisionDetectionResult enrichWithMarketPrices(
            VisionDetectionResult detection,
            List<MarketOffer> offers
    ) {
        if (offers.isEmpty()) {
            return detection;
        }

        final List<BigDecimal> pricedOffers = offers.stream()
                .map(MarketOffer::price)
                .filter(price -> price != null)
                .toList();
        if (pricedOffers.isEmpty()) {
            return detection;
        }

        final BigDecimal min = pricedOffers.stream()
                .min(Comparator.naturalOrder())
                .orElse(detection.estimatedMinPrice());
        final BigDecimal max = pricedOffers.stream()
                .max(Comparator.naturalOrder())
                .orElse(detection.estimatedMaxPrice());

        return new VisionDetectionResult(
                detection.title(),
                detection.description(),
                detection.brand(),
                detection.category(),
                min.min(detection.estimatedMinPrice()),
                max.max(detection.estimatedMaxPrice()),
                Math.min(0.95, detection.confidence() + 0.05),
                detection.searchKeywords(),
                detection.similarAlternatives()
        );
    }

    private Product createProductFromDetection(AnalysisRequest request, VisionDetectionResult detection) {
        return productRepository.save(Product.builder()
                .title(detection.title())
                .description(detection.description())
                .imageUrl(request.getImageUrl())
                .category(detection.category())
                .brand(detection.brand())
                .minPrice(detection.estimatedMinPrice())
                .maxPrice(detection.estimatedMaxPrice())
                .analysisRequestId(request.getId())
                .build());
    }

    private void createStoresForProduct(Product product, List<MarketOffer> offers) {
        for (MarketOffer offer : offers) {
            storeRepository.save(Store.builder()
                    .product(product)
                    .name(offer.storeName())
                    .url(offer.url())
                    .price(offer.price())
                    .inStock(true)
                    .build());
        }
    }

    private List<Product> findSimilarProducts(
            VisionDetectionResult detection,
            Product exact,
            UUID requestId
    ) {
        final List<Product> results = new ArrayList<>();

        for (String alternative : detection.similarAlternatives()) {
            if (alternative.isBlank() || results.size() >= 2) {
                continue;
            }

            results.add(productRepository.save(Product.builder()
                    .title(alternative)
                    .description(exact.getTitle() + " için alternatif seçenek")
                    .imageUrl(exact.getImageUrl())
                    .category(detection.category())
                    .brand(detection.brand())
                    .minPrice(detection.estimatedMinPrice().multiply(BigDecimal.valueOf(0.7))
                            .setScale(0, RoundingMode.HALF_UP))
                    .maxPrice(detection.estimatedMinPrice().multiply(BigDecimal.valueOf(0.95))
                            .setScale(0, RoundingMode.HALF_UP))
                    .analysisRequestId(requestId)
                    .build()));
        }

        if (results.size() >= 2) {
            return results;
        }

        for (String keyword : detection.searchKeywords()) {
            if (keyword.length() < 3 || results.size() >= 2) {
                continue;
            }

            final List<Product> matches = productRepository
                    .searchByCategory(keyword, detection.category(), PageRequest.of(0, 3))
                    .getContent();

            for (Product catalogProduct : matches) {
                if (catalogProduct.getId().equals(exact.getId()) || results.size() >= 2) {
                    continue;
                }

                results.add(productRepository.save(Product.builder()
                        .title("Benzer: " + catalogProduct.getTitle())
                        .description(catalogProduct.getDescription())
                        .imageUrl(catalogProduct.getImageUrl())
                        .category(catalogProduct.getCategory())
                        .brand(catalogProduct.getBrand())
                        .minPrice(scalePrice(catalogProduct.getMinPrice(), detection.estimatedMinPrice(), 0.85))
                        .maxPrice(scalePrice(catalogProduct.getMaxPrice(), detection.estimatedMaxPrice(), 0.9))
                        .analysisRequestId(requestId)
                        .build()));
            }
        }

        if (results.isEmpty() && detection.confidence() >= 0.55) {
            results.add(productRepository.save(Product.builder()
                    .title("Ekonomik alternatif")
                    .description(detection.title() + " için daha uygun fiyatlı seçenek")
                    .imageUrl(exact.getImageUrl())
                    .category(detection.category())
                    .brand(detection.brand())
                    .minPrice(detection.estimatedMinPrice().multiply(BigDecimal.valueOf(0.75))
                            .setScale(0, RoundingMode.HALF_UP))
                    .maxPrice(detection.estimatedMinPrice().multiply(BigDecimal.valueOf(0.95))
                            .setScale(0, RoundingMode.HALF_UP))
                    .analysisRequestId(requestId)
                    .build()));
        }

        return results;
    }

    private BigDecimal scalePrice(BigDecimal catalogPrice, BigDecimal fallback, double factor) {
        if (catalogPrice != null) {
            return catalogPrice.multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.HALF_UP);
        }
        return fallback.multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.HALF_UP);
    }
}
