package com.nereden.api.infrastructure.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProductPagePriceFetcher {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final RestClient restClient;

    public ProductPagePriceFetcher() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(8).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(12).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public BigDecimal fetchPrice(String productUrl) {
        if (productUrl == null || productUrl.isBlank()) {
            return null;
        }

        try {
            final String html = restClient.get()
                    .uri(URI.create(productUrl.trim()))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .retrieve()
                    .body(String.class);

            if (html == null || html.isBlank()) {
                return null;
            }

            final BigDecimal price = parsePriceFromHtml(html, productUrl);
            if (price != null) {
                log.info("Fetched product page price from {}: {}", hostOf(productUrl), price);
            }
            return price;
        } catch (Exception ex) {
            log.debug("Could not fetch product page price from {}: {}", productUrl, ex.getMessage());
            return null;
        }
    }

    private BigDecimal parsePriceFromHtml(String html, String productUrl) {
        final String lowerUrl = productUrl.toLowerCase(Locale.ROOT);
        final List<Pattern> patterns = new ArrayList<>();

        if (lowerUrl.contains("trendyol.com")) {
            patterns.add(Pattern.compile("\"sellingPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"discountedPrice\"\\s*:\\s*\\{[^}]*\"value\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"salePrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
        }
        if (lowerUrl.contains("hepsiburada.com")) {
            patterns.add(Pattern.compile("\"price\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"originalPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"discountedPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
        }
        if (lowerUrl.contains("amazon.com")) {
            patterns.add(Pattern.compile("\"priceAmount\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"priceToPay\"\\s*:\\s*\\{[^}]*\"amount\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("a-price-whole\">([0-9.,]+)"));
        }
        if (lowerUrl.contains("n11.com")) {
            patterns.add(Pattern.compile("\"displayPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
            patterns.add(Pattern.compile("\"price\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"));
        }

        patterns.add(Pattern.compile("\"lowPrice\"\\s*:\\s*\"?([0-9]+(?:[.,][0-9]+)?)\"?"));
        patterns.add(Pattern.compile("\"price\"\\s*:\\s*\"([0-9]+(?:[.,][0-9]+)?)\""));
        patterns.add(Pattern.compile("itemprop=\"price\"\\s+content=\"([0-9]+(?:[.,][0-9]+)?)\""));

        for (Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                final BigDecimal price = TurkishPriceParser.fromText(matcher.group(1));
                if (isReasonablePrice(price)) {
                    return price;
                }
            }
        }

        return null;
    }

    private boolean isReasonablePrice(BigDecimal price) {
        return price != null
                && price.compareTo(BigDecimal.valueOf(5)) >= 0
                && price.compareTo(BigDecimal.valueOf(1_000_000)) <= 0;
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (IllegalArgumentException ex) {
            return url;
        }
    }
}
