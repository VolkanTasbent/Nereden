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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SerpApiExactLinkFinder {

    private static final Pattern PRICE_PATTERN = Pattern.compile("[\\d.,]+");
    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public SerpApiExactLinkFinder(
            @Value("${app.market.serpapi.api-key:}") String apiKey,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://serpapi.com")
                .build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Map<String, MarketOffer> findExactLinks(VisionDetectionResult detection) {
        if (!isAvailable()) {
            return Map.of();
        }

        final Map<String, MarketOffer> offers = new LinkedHashMap<>();
        final String query = buildQuery(detection);

        for (StoreTarget store : STORES) {
            try {
                findForStore(store, query, detection).ifPresent(offer -> offers.put(store.name(), offer));
            } catch (Exception ex) {
                log.warn("SerpAPI exact link failed for {}", store.name(), ex);
            }
        }

        return offers;
    }

    private java.util.Optional<MarketOffer> findForStore(
            StoreTarget store,
            String query,
            VisionDetectionResult detection
    ) throws Exception {
        final String siteQuery = "site:" + store.domain() + " " + query;

        final String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("engine", "google")
                        .queryParam("q", siteQuery)
                        .queryParam("gl", "tr")
                        .queryParam("hl", "tr")
                        .queryParam("num", 8)
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .body(String.class);

        final JsonNode organic = objectMapper.readTree(response).path("organic_results");
        if (!organic.isArray()) {
            return java.util.Optional.empty();
        }

        for (JsonNode result : organic) {
            final String link = result.path("link").asText("").trim();
            final String title = result.path("title").asText(detection.title()).trim();

            if (!ProductPageUrlValidator.isProductPage(link, store.domain())) {
                continue;
            }

            return java.util.Optional.of(new MarketOffer(
                    store.name(),
                    title,
                    link,
                    detection.estimatedMinPrice(),
                    true
            ));
        }

        return java.util.Optional.empty();
    }

    public List<MarketOffer> findShoppingOffers(String query) {
        if (!isAvailable() || query == null || query.isBlank()) {
            return List.of();
        }

        try {
            final String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("engine", "google_shopping")
                            .queryParam("q", query)
                            .queryParam("gl", "tr")
                            .queryParam("hl", "tr")
                            .queryParam("num", 8)
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            return parseShoppingOffers(objectMapper.readTree(response));
        } catch (Exception ex) {
            log.warn("SerpAPI shopping search failed for query '{}'", query, ex);
            return List.of();
        }
    }

    private List<MarketOffer> parseShoppingOffers(JsonNode root) {
        final List<MarketOffer> offers = new ArrayList<>();
        final JsonNode results = root.path("shopping_results");

        if (!results.isArray()) {
            return offers;
        }

        for (JsonNode item : results) {
            final String title = item.path("title").asText("").trim();
            final String source = item.path("source").asText("Mağaza").trim();
            final String link = item.path("link").asText("").trim();
            final BigDecimal price = parsePrice(item.path("price").asText(""));

            if (title.isBlank() || link.isBlank() || price == null) {
                continue;
            }

            final boolean direct = !ProductPageUrlValidator.isSearchPage(link);
            offers.add(new MarketOffer(source, title, link, price, direct));
            if (offers.size() >= 5) {
                break;
            }
        }

        return offers;
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw.replaceAll("[^\\d.,]", "").trim();
        if (cleaned.isBlank()) {
            return null;
        }

        if (cleaned.contains(",")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else {
            cleaned = cleaned.replace(".", "");
        }

        try {
            return new BigDecimal(cleaned).setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            final Matcher matcher = PRICE_PATTERN.matcher(raw.replace(" ", ""));
            if (!matcher.find()) {
                return null;
            }
            try {
                return new BigDecimal(matcher.group().replace(",", ".")).setScale(0, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private String buildQuery(VisionDetectionResult detection) {
        if (detection.brand() != null && !detection.brand().isBlank()) {
            return detection.brand() + " " + detection.title();
        }
        return detection.title();
    }
}
