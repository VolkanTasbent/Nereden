package com.nereden.api.infrastructure.market;

import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class EstimatedMarketSearchProvider implements MarketSearchProvider {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<MarketOffer> search(String query) {
        return List.of();
    }

    public List<MarketOffer> buildFromDetection(VisionDetectionResult detection) {
        final List<MarketOffer> offers = new ArrayList<>();
        for (StoreTarget store : List.of(
                new StoreTarget("Trendyol", "trendyol.com"),
                new StoreTarget("Hepsiburada", "hepsiburada.com"),
                new StoreTarget("Amazon TR", "amazon.com.tr"),
                new StoreTarget("N11", "n11.com")
        )) {
            offers.add(buildForStore(store, detection));
        }
        return offers;
    }

    public MarketOffer buildForStore(StoreTarget store, VisionDetectionResult detection) {
        final String encodedQuery = encodeExactQuery(detection);
        final String url = switch (store.name()) {
            case "Trendyol" -> "https://www.trendyol.com/sr?q=" + encodedQuery;
            case "Hepsiburada" -> "https://www.hepsiburada.com/ara?q=" + encodedQuery;
            case "Amazon TR" -> "https://www.amazon.com.tr/s?k=" + encodedQuery;
            case "N11" -> "https://www.n11.com/arama?q=" + encodedQuery;
            default -> "https://www.google.com/search?q=" + encodedQuery;
        };

        return new MarketOffer(
                store.name(),
                detection.title(),
                url,
                null,
                false
        );
    }

    private String encodeExactQuery(VisionDetectionResult detection) {
        final String exactTitle = detection.title().trim();
        final String query = detection.brand() != null && !detection.brand().isBlank()
                ? "\"" + exactTitle + "\" " + detection.brand()
                : "\"" + exactTitle + "\"";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
