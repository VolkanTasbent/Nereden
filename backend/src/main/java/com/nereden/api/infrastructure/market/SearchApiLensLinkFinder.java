package com.nereden.api.infrastructure.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SearchApiLensLinkFinder {

    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final LensPublicImageResolver lensPublicImageResolver;
    private final String apiKey;

    public SearchApiLensLinkFinder(
            @Value("${app.market.searchapi.api-key:}") String apiKey,
            ObjectMapper objectMapper,
            LensPublicImageResolver lensPublicImageResolver
    ) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.lensPublicImageResolver = lensPublicImageResolver;
        this.restClient = RestClient.builder()
                .baseUrl("https://www.searchapi.io/api/v1")
                .build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Map<String, MarketOffer> findExactLinks(String imageUrl, VisionDetectionResult detection) {
        if (!isAvailable()) {
            return Map.of();
        }

        final String lensImageUrl = lensPublicImageResolver.resolvePublicUrl(imageUrl);
        if (lensImageUrl == null) {
            log.warn("Google Lens skipped — image is not publicly accessible");
            return Map.of();
        }

        final Map<String, MarketOffer> offers = new LinkedHashMap<>();
        final String lensQuery = buildLensQuery(detection);

        try {
            collectOffers(searchLens(lensImageUrl, "products", lensQuery), offers, detection);
            if (offers.isEmpty()) {
                collectOffers(searchLens(lensImageUrl, "exact_matches", null), offers, detection);
            }
            log.info("SearchAPI Google Lens found {} direct store links", offers.size());
        } catch (Exception ex) {
            log.warn("SearchAPI Google Lens failed", ex);
        }

        return offers;
    }

    private JsonNode searchLens(String imageUrl, String searchType, String query) throws Exception {
        final String response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/search")
                            .queryParam("engine", "google_lens")
                            .queryParam("search_type", searchType)
                            .queryParam("url", imageUrl)
                            .queryParam("country", "TR")
                            .queryParam("hl", "tr")
                            .queryParam("api_key", apiKey);
                    if (query != null && !query.isBlank()) {
                        uriBuilder.queryParam("q", query);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(String.class);

        return objectMapper.readTree(response);
    }

    private void collectOffers(JsonNode root, Map<String, MarketOffer> offers, VisionDetectionResult detection) {
        final List<ScoredMatch> candidates = new ArrayList<>();
        parseMatches(root.path("exact_matches"), candidates, detection);
        parseMatches(root.path("visual_matches"), candidates, detection);

        candidates.sort(Comparator.comparingDouble(ScoredMatch::score).reversed());

        for (ScoredMatch candidate : candidates) {
            if (offers.containsKey(candidate.storeName())) {
                continue;
            }
            offers.put(candidate.storeName(), candidate.offer());
        }
    }

    private void parseMatches(JsonNode matches, List<ScoredMatch> candidates, VisionDetectionResult detection) {
        if (!matches.isArray()) {
            return;
        }

        for (JsonNode match : matches) {
            final String link = match.path("link").asText("").trim();
            final String title = match.path("title").asText(detection.title()).trim();
            final String source = match.path("source").asText("").trim();
            final BigDecimal price = parsePrice(match);

            if (link.isBlank()) {
                continue;
            }

            final double titleScore = ProductTitleMatcher.score(detection.title(), title, detection.brand());
            if (!ProductTitleMatcher.isGoodMatch(detection.title(), title, detection.brand())) {
                continue;
            }

            for (StoreTarget store : STORES) {
                if (!link.toLowerCase().contains(store.domain())
                        && !source.toLowerCase().contains(store.name().toLowerCase().split(" ")[0])) {
                    continue;
                }
                if (!ProductPageUrlValidator.isProductPage(link, store.domain())) {
                    continue;
                }

                candidates.add(new ScoredMatch(
                        store.name(),
                        titleScore,
                        new MarketOffer(store.name(), title, link, price, true)
                ));
            }
        }
    }

    private BigDecimal parsePrice(JsonNode match) {
        return TurkishPriceParser.fromJsonNode(match);
    }

    private String buildLensQuery(VisionDetectionResult detection) {
        if (detection.brand() != null && !detection.brand().isBlank()) {
            return detection.brand() + " Trendyol";
        }
        return "Trendyol";
    }

    private record ScoredMatch(String storeName, double score, MarketOffer offer) {}
}
