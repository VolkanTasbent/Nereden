package com.nereden.api.infrastructure.market;

import java.net.URI;
import java.util.Locale;

public final class ProductUrlMatcher {

    private ProductUrlMatcher() {}

    public static boolean urlsLikelySameProduct(String left, String right) {
        if (left == null || right == null || left.isBlank() || right.isBlank()) {
            return false;
        }

        final String normalizedLeft = normalizeUrl(left);
        final String normalizedRight = normalizeUrl(right);
        if (normalizedLeft.equals(normalizedRight)) {
            return true;
        }

        final String leftId = extractProductToken(left);
        final String rightId = extractProductToken(right);
        return !leftId.isBlank() && leftId.equals(rightId);
    }

    public static String extractProductToken(String url) {
        final String lower = url.toLowerCase(Locale.ROOT);
        final int trendyolIndex = lower.lastIndexOf("-p-");
        if (trendyolIndex >= 0) {
            return lower.substring(trendyolIndex);
        }
        final int hepsiburadaIndex = lower.lastIndexOf("-pm-");
        if (hepsiburadaIndex >= 0) {
            return lower.substring(hepsiburadaIndex);
        }
        final int amazonIndex = lower.indexOf("/dp/");
        if (amazonIndex >= 0) {
            final String tail = lower.substring(amazonIndex);
            final int end = tail.indexOf('/', 4);
            return end > 0 ? tail.substring(0, end) : tail;
        }
        if (lower.contains("n11.com/urun/")) {
            final int start = lower.lastIndexOf('-');
            if (start >= 0) {
                return lower.substring(start);
            }
        }
        return "";
    }

    private static String normalizeUrl(String url) {
        try {
            final URI uri = URI.create(url.trim());
            final String host = uri.getHost() != null ? uri.getHost().toLowerCase(Locale.ROOT) : "";
            final String path = uri.getPath() != null ? uri.getPath().toLowerCase(Locale.ROOT) : "";
            return host + path;
        } catch (IllegalArgumentException ex) {
            return url.toLowerCase(Locale.ROOT);
        }
    }
}
