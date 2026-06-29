package com.nereden.api.infrastructure.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.GeminiApiService;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiExactLinkFinder {

    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final GeminiApiService geminiApiService;

    public Map<String, MarketOffer> findExactLinks(VisionDetectionResult detection) {
        if (!geminiApiService.isAvailable()) {
            return Map.of();
        }

        try {
            final JsonNode response = geminiApiService.generateWithGoogleSearch(
                    buildPrompt(detection),
                    geminiApiService.flashModels(),
                    0.1
            );

            final Map<String, MarketOffer> offers = new LinkedHashMap<>();
            collectFromGrounding(response, detection, offers);
            collectFromText(response, detection, offers);
            return offers;
        } catch (Exception ex) {
            log.warn("Gemini exact link search failed", ex);
            return Map.of();
        }
    }

    private void collectFromGrounding(
            JsonNode response,
            VisionDetectionResult detection,
            Map<String, MarketOffer> offers
    ) {
        final JsonNode chunks = response.path("candidates")
                .path(0)
                .path("groundingMetadata")
                .path("groundingChunks");

        if (!chunks.isArray()) {
            return;
        }

        for (StoreTarget store : STORES) {
            if (offers.containsKey(store.name())) {
                continue;
            }

            for (JsonNode chunk : chunks) {
                final String uri = chunk.path("web").path("uri").asText("").trim();
                final String title = chunk.path("web").path("title").asText(detection.title()).trim();

                if (!ProductPageUrlValidator.isProductPage(uri, store.domain())) {
                    continue;
                }

                offers.put(store.name(), new MarketOffer(
                        store.name(),
                        title,
                        uri,
                        null,
                        true
                ));
                break;
            }
        }
    }

    private void collectFromText(
            JsonNode response,
            VisionDetectionResult detection,
            Map<String, MarketOffer> offers
    ) {
        final String text = response.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText("");

        if (text.isBlank()) {
            return;
        }

        for (StoreTarget store : STORES) {
            if (offers.containsKey(store.name())) {
                continue;
            }

            findUrlInText(text, store.domain())
                    .filter(url -> ProductPageUrlValidator.isProductPage(url, store.domain()))
                    .ifPresent(url -> offers.put(store.name(), new MarketOffer(
                            store.name(),
                            detection.title(),
                            url,
                            null,
                            true
                    )));
        }
    }

    private Optional<String> findUrlInText(String text, String domain) {
        final String[] tokens = text.split("\\s");
        for (String token : tokens) {
            final String cleaned = token.replaceAll("[\"'<>\\]),]", "").trim();
            if (cleaned.startsWith("http") && cleaned.contains(domain)) {
                return Optional.of(cleaned);
            }
        }
        return Optional.empty();
    }

    private String buildPrompt(VisionDetectionResult detection) {
        final String brand = detection.brand() != null ? detection.brand() : "bilinmiyor";
        return """
                Türkiye e-ticaret sitelerinde bu ürünün DOĞRUDAN ürün sayfası URL'lerini bul.
                Arama sonuç sayfası URL'si verme — sadece tek ürün sayfası.

                Ürün: %s
                Marka: %s
                Anahtar kelimeler: %s

                Trendyol, Hepsiburada, Amazon TR ve N11 için mümkünse doğrudan ürün linklerini metinde paylaş.
                """.formatted(
                detection.title(),
                brand,
                String.join(", ", detection.searchKeywords())
        );
    }
}
