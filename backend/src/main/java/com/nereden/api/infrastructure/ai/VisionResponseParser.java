package com.nereden.api.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nereden.api.domain.entity.ProductCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class VisionResponseParser {

    private final ObjectMapper objectMapper;

    public VisionResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public VisionDetectionResult parseLegacyCombined(String jsonContent) {
        try {
            final JsonNode json = objectMapper.readTree(jsonContent);
            final ProductCategory category = parseCategory(json.path("category").asText("other"));
            final BigDecimal minPrice = normalizePrice(json.path("estimatedMinPrice").asDouble(0), category, true);
            final BigDecimal maxPrice = normalizePrice(json.path("estimatedMaxPrice").asDouble(0), category, false);
            final BigDecimal[] range = ensureValidRange(minPrice, maxPrice, category);

            return new VisionDetectionResult(
                    sanitizeTitle(json.path("title").asText("Tespit Edilen Ürün")),
                    json.path("description").asText(""),
                    parseBrand(json.path("brand")),
                    category,
                    range[0],
                    range[1],
                    clampConfidence(json.path("confidence").asDouble(0.75)),
                    parseKeywords(json.path("searchKeywords")),
                    parseStringList(json.path("similarAlternatives"))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("AI yanıtı işlenemedi.", ex);
        }
    }

    public static String combinedAnalysisPrompt() {
        return """
                Sen bir ürün tanıma ve fiyat uzmanısın. Görselde GERÇEKTEN gördüğün tek ana ürünü analiz et.
                Tahmin etme, uydurma. Marka logosu yoksa brand=null.
                Türkiye e-ticaret fiyatları (Trendyol, Hepsiburada — Haziran 2026).

                Sadece JSON:
                {
                  "title": "spesifik ürün adı (tür + renk + özellik)",
                  "description": "1 cümle",
                  "brand": null,
                  "category": "fashion|furniture|electronics|decoration|accessories|other",
                  "confidence": 0.85,
                  "estimatedMinPrice": 0,
                  "estimatedMaxPrice": 0,
                  "searchKeywords": ["4-6 Türkçe arama kelimesi"],
                  "similarAlternatives": ["2 alternatif ürün"]
                }
                """;
    }

    public static String legacyCombinedPrompt() {
        return combinedAnalysisPrompt();
    }

    public ProductIdentification parseIdentification(String jsonContent) {
        try {
            final JsonNode json = objectMapper.readTree(jsonContent);
            return new ProductIdentification(
                    sanitizeTitle(json.path("title").asText("Tespit Edilen Ürün")),
                    json.path("description").asText(""),
                    parseBrand(json.path("brand")),
                    parseCategory(json.path("category").asText("other")),
                    clampConfidence(json.path("confidence").asDouble(0.7)),
                    json.path("color").asText(""),
                    json.path("material").asText(""),
                    json.path("productType").asText("")
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Ürün tanıma yanıtı işlenemedi.", ex);
        }
    }

    public PriceEstimation parsePriceEstimation(String jsonContent, ProductCategory category) {
        try {
            final JsonNode json = objectMapper.readTree(jsonContent);
            final BigDecimal minPrice = normalizePrice(json.path("estimatedMinPrice").asDouble(0), category, true);
            final BigDecimal maxPrice = normalizePrice(json.path("estimatedMaxPrice").asDouble(0), category, false);
            final BigDecimal[] range = ensureValidRange(minPrice, maxPrice, category);

            return new PriceEstimation(
                    range[0],
                    range[1],
                    parseKeywords(json.path("searchKeywords")),
                    parseStringList(json.path("similarAlternatives")),
                    json.path("priceReasoning").asText("")
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Fiyat tahmini yanıtı işlenemedi.", ex);
        }
    }

    public VisionDetectionResult merge(ProductIdentification identification, PriceEstimation pricing) {
        final double confidence = Math.min(identification.confidence(), estimatePriceConfidence(pricing));
        return new VisionDetectionResult(
                identification.title(),
                identification.description(),
                identification.brand(),
                identification.category(),
                pricing.estimatedMinPrice(),
                pricing.estimatedMaxPrice(),
                confidence,
                pricing.searchKeywords(),
                pricing.similarAlternatives()
        );
    }

    public static String identificationPrompt() {
        return """
                Sen bir ürün tanıma uzmanısın. Görselde GERÇEKTEN gördüğün tek ana ürünü analiz et.
                Tahmin etme, uydurma, alakasız ürün yazma. Marka logosu yoksa brand=null.

                Sadece JSON:
                {
                  "title": "spesifik ürün adı (tür + renk + özellik)",
                  "description": "1 cümle, sadece görünenler",
                  "brand": null,
                  "category": "fashion|furniture|electronics|decoration|accessories|other",
                  "confidence": 0.85,
                  "color": "renk",
                  "material": "malzeme veya boş",
                  "productType": "ürün türü (ör: sneaker, kolonya, sehpa)"
                }
                """;
    }

    public static String priceEstimationPrompt(ProductIdentification identification) {
        return """
                Türkiye e-ticaret uzmanısın (Trendyol, Hepsiburada, Amazon TR — Haziran 2026).
                Aşağıdaki ürün için gerçekçi TRY fiyat aralığı ve arama kelimeleri üret.

                Ürün bilgisi:
                - Ad: %s
                - Açıklama: %s
                - Marka: %s
                - Kategori: %s
                - Renk: %s
                - Malzeme: %s
                - Tür: %s

                Kurallar:
                1. estimatedMinPrice / estimatedMaxPrice: Türkiye'deki tipik satış fiyat aralığı (TRY, tam sayı)
                2. searchKeywords: Trendyol'da aranacak 4-6 Türkçe kelime (marka varsa dahil et)
                3. similarAlternatives: 2 alternatif ürün önerisi (daha ucuz/benzer, Türkçe kısa başlık)
                4. priceReasoning: fiyat aralığını 1 cümlede açıkla

                Sadece JSON:
                {
                  "estimatedMinPrice": 0,
                  "estimatedMaxPrice": 0,
                  "searchKeywords": ["..."],
                  "similarAlternatives": ["...", "..."],
                  "priceReasoning": "..."
                }
                """.formatted(
                identification.title(),
                identification.description(),
                identification.brand() != null ? identification.brand() : "bilinmiyor",
                identification.category().name().toLowerCase(),
                blankToDash(identification.color()),
                blankToDash(identification.material()),
                blankToDash(identification.productType())
        );
    }

    public static String refinementPrompt(ProductIdentification previous) {
        return """
                Önceki analiz belirsizdi. Görseli TEKRAR incele ve düzelt.
                Önceki sonuç: %s (%s) — güven: %.2f

                Görseldeki gerçek ürünü daha dikkatli tanımla. Sadece JSON:
                {
                  "title": "spesifik ürün adı",
                  "description": "1 cümle",
                  "brand": null,
                  "category": "fashion|furniture|electronics|decoration|accessories|other",
                  "confidence": 0.85,
                  "color": "renk",
                  "material": "malzeme",
                  "productType": "ürün türü"
                }
                """.formatted(previous.title(), previous.description(), previous.confidence());
    }

    private double estimatePriceConfidence(PriceEstimation pricing) {
        if (pricing.priceReasoning() != null && pricing.priceReasoning().length() > 20) {
            return 0.85;
        }
        return 0.75;
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String sanitizeTitle(String title) {
        final String trimmed = title == null ? "" : title.trim();
        return trimmed.isBlank() ? "Tespit Edilen Ürün" : trimmed;
    }

    private String parseBrand(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        final String brand = node.asText("").trim();
        return brand.isBlank() || "null".equalsIgnoreCase(brand) ? null : brand;
    }

    private double clampConfidence(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private BigDecimal normalizePrice(double value, ProductCategory category, boolean min) {
        if (value <= 0) {
            return defaultPrice(category, min);
        }
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal[] ensureValidRange(BigDecimal min, BigDecimal max, ProductCategory category) {
        BigDecimal low = min;
        BigDecimal high = max;

        if (low.compareTo(high) > 0) {
            final BigDecimal temp = low;
            low = high;
            high = temp;
        }

        if (high.subtract(low).compareTo(BigDecimal.TEN) < 0) {
            high = low.multiply(BigDecimal.valueOf(1.3)).setScale(0, RoundingMode.HALF_UP);
        }

        return new BigDecimal[]{low, high};
    }

    private BigDecimal defaultPrice(ProductCategory category, boolean min) {
        return switch (category) {
            case FASHION -> min ? new BigDecimal("299") : new BigDecimal("899");
            case FURNITURE -> min ? new BigDecimal("999") : new BigDecimal("4999");
            case ELECTRONICS -> min ? new BigDecimal("999") : new BigDecimal("9999");
            case DECORATION -> min ? new BigDecimal("99") : new BigDecimal("499");
            case ACCESSORIES -> min ? new BigDecimal("199") : new BigDecimal("1499");
            default -> min ? new BigDecimal("199") : new BigDecimal("999");
        };
    }

    private ProductCategory parseCategory(String value) {
        try {
            return ProductCategory.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            return ProductCategory.OTHER;
        }
    }

    private List<String> parseKeywords(JsonNode node) {
        return dedupeStrings(parseStringList(node), 6);
    }

    private List<String> parseStringList(JsonNode node) {
        final List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                final String value = n.asText("").trim();
                if (!value.isBlank()) {
                    values.add(value);
                }
            });
        }
        return values;
    }

    private List<String> dedupeStrings(List<String> values, int max) {
        final Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value.length() >= 2) {
                unique.add(value);
            }
            if (unique.size() >= max) {
                break;
            }
        }
        return unique.isEmpty() ? List.of("ürün") : new ArrayList<>(unique);
    }
}
