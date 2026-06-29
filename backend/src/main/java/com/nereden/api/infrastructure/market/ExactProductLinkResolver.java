package com.nereden.api.infrastructure.market;

import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExactProductLinkResolver {

    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final SearchApiLensLinkFinder searchApiLensLinkFinder;
    private final SearchApiGoogleLinkFinder searchApiGoogleLinkFinder;
    private final GeminiExactLinkFinder geminiExactLinkFinder;
    private final StoreOfferEnricher storeOfferEnricher;

    public List<MarketOffer> resolve(VisionDetectionResult detection, String imageUrl) {
        final Map<String, MarketOffer> resolved = new LinkedHashMap<>();
        final String query = buildQuery(detection);
        final List<MarketOffer> shoppingOffers = searchApiGoogleLinkFinder.findShoppingOffers(query, detection);

        searchApiLensLinkFinder.findExactLinks(imageUrl, detection).forEach(resolved::putIfAbsent);

        if (resolved.size() < STORES.size()) {
            searchApiGoogleLinkFinder.findExactLinks(detection, resolved.keySet()).forEach(resolved::putIfAbsent);
        }

        if (resolved.size() < 2) {
            geminiExactLinkFinder.findExactLinks(detection).forEach(resolved::putIfAbsent);
        }

        searchApiGoogleLinkFinder.findDirectLinksFromShopping(shoppingOffers, resolved.keySet(), detection)
                .forEach(resolved::putIfAbsent);

        final List<MarketOffer> results = new ArrayList<>();
        for (StoreTarget store : STORES) {
            final MarketOffer exact = resolved.get(store.name());
            if (exact == null || !isDirectProductOffer(exact, store)) {
                continue;
            }

            final MarketOffer enriched = storeOfferEnricher.enrich(exact, shoppingOffers, detection);
            log.info("Direct product link for {}: {} (price: {})",
                    store.name(), enriched.url(), enriched.price());
            results.add(enriched);
        }

        log.info("Found {} stores selling this product", results.size());
        return results;
    }

    private boolean isDirectProductOffer(MarketOffer offer, StoreTarget store) {
        return offer.directProductLink()
                && ProductPageUrlValidator.isProductPage(offer.url(), store.domain());
    }

    private String buildQuery(VisionDetectionResult detection) {
        if (!detection.searchKeywords().isEmpty()) {
            return String.join(" ", detection.searchKeywords().subList(0, Math.min(4, detection.searchKeywords().size())));
        }
        if (detection.brand() != null && !detection.brand().isBlank()) {
            return detection.brand() + " " + detection.title();
        }
        return detection.title();
    }
}
