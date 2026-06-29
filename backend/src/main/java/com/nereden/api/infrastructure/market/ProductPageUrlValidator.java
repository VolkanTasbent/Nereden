package com.nereden.api.infrastructure.market;

public final class ProductPageUrlValidator {

    private ProductPageUrlValidator() {}

    public static boolean isProductPage(String url, String domain) {
        if (url == null || url.isBlank() || domain == null) {
            return false;
        }

        final String lower = url.toLowerCase();
        final String domainLower = domain.toLowerCase();

        if (!lower.contains(domainLower)) {
            return false;
        }

        if (isSearchPageInternal(lower)) {
            return false;
        }

        return switch (domainLower) {
            case "trendyol.com" -> lower.contains("-p-") || lower.matches(".*trendyol\\.com/[^/]+/[^/?]+.*");
            case "hepsiburada.com" -> lower.contains("-p-") || lower.contains("-pm-");
            case "amazon.com.tr" -> lower.contains("/dp/") || lower.contains("/gp/product/");
            case "n11.com" -> lower.contains("/urun/") || lower.contains("productid=");
            default -> !isSearchPageInternal(lower);
        };
    }

    public static boolean isSearchPage(String url) {
        if (url == null) {
            return true;
        }
        return isSearchPageInternal(url.toLowerCase());
    }

    private static boolean isSearchPageInternal(String lower) {
        return lower.contains("/sr?q=")
                || lower.contains("/ara?q=")
                || lower.contains("/s?k=")
                || lower.contains("/arama?")
                || lower.contains("google.com/search")
                || lower.contains("/search?");
    }
}
