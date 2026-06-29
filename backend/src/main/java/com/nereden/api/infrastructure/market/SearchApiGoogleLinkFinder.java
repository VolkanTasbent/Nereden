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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SearchApiGoogleLinkFinder {

    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public SearchApiGoogleLinkFinder(
            @Value("${app.market.searchapi.api-key:}") String apiKey,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://www.searchapi.io/api/v1")
                .build();
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Map<String, MarketOffer> findExactLinks(VisionDetectionResult detection) {
        return findExactLinks(detection, Set.of());
    }

    public Map<String, MarketOffer> findExactLinks(VisionDetectionResult detection, Set<String> skipStores) {
        if (!isAvailable()) {
            return Map.of();
        }

        final Map<String, MarketOffer> offers = new LinkedHashMap<>();
        final String query = buildQuery(detection);

        final List<CompletableFuture<Void>> tasks = STORES.stream()
                .filter(store -> !skipStores.contains(store.name()))
                .map(store -> CompletableFuture.runAsync(() -> {
                    try {
                        findForStore(store, query, detection)
                                .ifPresent(offer -> synchronizedPut(offers, store.name(), offer));
                    } catch (Exception ex) {
                        log.warn("SearchAPI exact link failed for {}", store.name(), ex);
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        return offers;
    }

    public List<MarketOffer> findShoppingOffers(String query, VisionDetectionResult detection) {
        if (!isAvailable() || query == null || query.isBlank()) {
            return List.of();
        }

        try {
            final String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("engine", "google_shopping")
                            .queryParam("q", query)
                            .queryParam("gl", "tr")
                            .queryParam("hl", "tr")
                            .queryParam("location", "Turkey")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            return parseShoppingOffers(objectMapper.readTree(response), detection);
        } catch (Exception ex) {
            log.warn("SearchAPI shopping search failed for query '{}'", query, ex);
            return List.of();
        }
    }

    public List<MarketOffer> findShoppingOffersRelaxed(String query) {
        if (!isAvailable() || query == null || query.isBlank()) {
            return List.of();
        }

        try {
            final String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("engine", "google_shopping")
                            .queryParam("q", query)
                            .queryParam("gl", "tr")
                            .queryParam("hl", "tr")
                            .queryParam("location", "Turkey")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            return parseShoppingOffersRelaxed(objectMapper.readTree(response));
        } catch (Exception ex) {
            log.warn("SearchAPI relaxed shopping search failed for query '{}'", query, ex);
            return List.of();
        }
    }

    public Map<String, MarketOffer> findDirectLinksFromShopping(
            List<MarketOffer> shoppingOffers,
            Set<String> skipStores,
            VisionDetectionResult detection
    ) {
        final Map<String, MarketOffer> offers = new LinkedHashMap<>();

        for (MarketOffer offer : shoppingOffers) {
            if (skipStores.contains(offer.storeName())) {
                continue;
            }
            if (ProductPageUrlValidator.isSearchPage(offer.url())) {
                continue;
            }

            final StoreTarget store = STORES.stream()
                    .filter(target -> target.name().equals(offer.storeName()))
                    .findFirst()
                    .orElse(null);
            if (store == null || !ProductPageUrlValidator.isProductPage(offer.url(), store.domain())) {
                continue;
            }

            if (!ProductTitleMatcher.isGoodMatch(detection.title(), offer.productTitle(), detection.brand())) {
                continue;
            }

            final MarketOffer direct = new MarketOffer(
                    offer.storeName(),
                    offer.productTitle(),
                    offer.url(),
                    offer.price(),
                    true
            );
            final MarketOffer existing = offers.get(offer.storeName());
            if (existing == null || ProductTitleMatcher.score(detection.title(), offer.productTitle(), detection.brand())
                    > ProductTitleMatcher.score(detection.title(), existing.productTitle(), detection.brand())) {
                offers.put(offer.storeName(), direct);
            }
        }

        return offers;
    }

    public Optional<BigDecimal> findShoppingPriceForProduct(MarketOffer offer, VisionDetectionResult detection) {
        if (!isAvailable() || offer.url() == null || offer.url().isBlank()) {
            return Optional.empty();
        }

        final String titleQuery = offer.productTitle() != null && !offer.productTitle().isBlank()
                ? offer.productTitle()
                : detection.title();

        final List<MarketOffer> offers = findShoppingOffersRelaxed(titleQuery);
        final String productToken = ProductUrlMatcher.extractProductToken(offer.url());

        final Optional<BigDecimal> sameUrlPrice = offers.stream()
                .filter(candidate -> offer.storeName().equals(candidate.storeName()))
                .filter(candidate -> ProductUrlMatcher.urlsLikelySameProduct(offer.url(), candidate.url()))
                .map(MarketOffer::price)
                .filter(price -> price != null)
                .findFirst();
        if (sameUrlPrice.isPresent()) {
            return sameUrlPrice;
        }

        if (!productToken.isBlank()) {
            final Optional<BigDecimal> tokenPrice = offers.stream()
                    .filter(candidate -> offer.storeName().equals(candidate.storeName()))
                    .filter(candidate -> candidate.url().toLowerCase(Locale.ROOT).contains(productToken))
                    .map(MarketOffer::price)
                    .filter(price -> price != null)
                    .findFirst();
            if (tokenPrice.isPresent()) {
                return tokenPrice;
            }
        }

        return offers.stream()
                .filter(candidate -> offer.storeName().equals(candidate.storeName()))
                .filter(candidate -> ProductTitleMatcher.isGoodMatch(
                        detection.title(),
                        candidate.productTitle(),
                        detection.brand()))
                .map(MarketOffer::price)
                .filter(price -> price != null)
                .findFirst();
    }

    private synchronized void synchronizedPut(Map<String, MarketOffer> offers, String key, MarketOffer offer) {
        offers.putIfAbsent(key, offer);
    }

    private Optional<MarketOffer> findForStore(
            StoreTarget store,
            String query,
            VisionDetectionResult detection
    ) throws Exception {
        final String siteQuery = "site:" + store.domain() + " " + query;

        final String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("engine", "google")
                        .queryParam("q", siteQuery)
                        .queryParam("gl", "tr")
                        .queryParam("hl", "tr")
                        .queryParam("num", 10)
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .body(String.class);

        final JsonNode organic = objectMapper.readTree(response).path("organic_results");
        if (!organic.isArray()) {
            return Optional.empty();
        }

        MarketOffer bestMatch = null;
        double bestScore = 0;

        for (JsonNode result : organic) {
            final String link = result.path("link").asText("").trim();
            final String title = result.path("title").asText(detection.title()).trim();

            if (!ProductPageUrlValidator.isProductPage(link, store.domain())) {
                continue;
            }

            final double titleScore = ProductTitleMatcher.score(detection.title(), title, detection.brand());
            if (!ProductTitleMatcher.isGoodMatch(detection.title(), title, detection.brand())) {
                continue;
            }

            if (titleScore > bestScore) {
                bestScore = titleScore;
                bestMatch = new MarketOffer(store.name(), title, link, null, true);
            }
        }

        return Optional.ofNullable(bestMatch);
    }

    private List<MarketOffer> parseShoppingOffers(JsonNode root, VisionDetectionResult detection) {
        final List<MarketOffer> offers = new ArrayList<>();
        final JsonNode results = root.path("shopping_results");

        if (!results.isArray()) {
            return List.of();
        }

        for (JsonNode item : results) {
            final String title = item.path("title").asText("").trim();
            final String source = item.path("seller").asText(
                    item.path("source").asText("Mağaza")
            ).trim();
            final String link = firstNonBlank(
                    item.path("link").asText(""),
                    item.path("product_link").asText("")
            ).trim();
            final BigDecimal price = TurkishPriceParser.fromJsonNode(item);
            final String storeName = resolveStoreName(source, link);

            if (title.isBlank() || link.isBlank() || price == null || storeName == null) {
                continue;
            }

            if (!ProductTitleMatcher.isGoodMatch(detection.title(), title, detection.brand())) {
                continue;
            }

            final boolean direct = !ProductPageUrlValidator.isSearchPage(link);
            offers.add(new MarketOffer(storeName, title, link, price, direct));
        }

        return offers;
    }

    private List<MarketOffer> parseShoppingOffersRelaxed(JsonNode root) {
        final List<MarketOffer> offers = new ArrayList<>();
        final JsonNode results = root.path("shopping_results");

        if (!results.isArray()) {
            return List.of();
        }

        for (JsonNode item : results) {
            final String title = item.path("title").asText("").trim();
            final String source = item.path("seller").asText(
                    item.path("source").asText("Mağaza")
            ).trim();
            final String link = firstNonBlank(
                    item.path("link").asText(""),
                    item.path("product_link").asText("")
            ).trim();
            final BigDecimal price = TurkishPriceParser.fromJsonNode(item);
            final String storeName = resolveStoreName(source, link);

            if (title.isBlank() || link.isBlank() || price == null || storeName == null) {
                continue;
            }

            final boolean direct = !ProductPageUrlValidator.isSearchPage(link);
            offers.add(new MarketOffer(storeName, title, link, price, direct));
        }

        return offers;
    }

    private String resolveStoreName(String seller, String link) {
        final String haystack = (seller + " " + link).toLowerCase(Locale.ROOT);
        for (StoreTarget store : STORES) {
            final String token = store.name().toLowerCase(Locale.ROOT).split(" ")[0];
            if (haystack.contains(store.domain()) || haystack.contains(token)) {
                return store.name();
            }
        }
        return null;
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
