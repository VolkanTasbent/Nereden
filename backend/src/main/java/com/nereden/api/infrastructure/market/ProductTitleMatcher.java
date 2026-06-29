package com.nereden.api.infrastructure.market;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ProductTitleMatcher {

    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "(\\d+)\\s*(adet|adt|li|lı|lu|lü|ml|gr|g|kg|lt|l|pack|paket|parça|parca)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "ve", "ile", "icin", "için", "the", "and", "for", "renk", "renkli", "erkek", "kadin", "kadın",
            "kol", "saati", "saat", "urun", "ürün", "yeni", "orijinal", "adet", "set"
    );

    private ProductTitleMatcher() {}

    public static double score(String detectedTitle, String candidateTitle, String brand) {
        if (candidateTitle == null || candidateTitle.isBlank()) {
            return 0;
        }

        final Set<String> detectedTokens = tokenize(detectedTitle);
        final Set<String> candidateTokens = tokenize(candidateTitle);
        if (detectedTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0;
        }

        final Set<String> intersection = new HashSet<>(detectedTokens);
        intersection.retainAll(candidateTokens);

        final Set<String> union = new HashSet<>(detectedTokens);
        union.addAll(candidateTokens);

        double score = union.isEmpty() ? 0 : (double) intersection.size() / union.size();

        if (brand != null && !brand.isBlank()) {
            final String brandNorm = normalize(brand);
            if (normalize(candidateTitle).contains(brandNorm)) {
                score += 0.25;
            }
        }

        return Math.min(1.0, score);
    }

    public static boolean isGoodMatch(String detectedTitle, String candidateTitle, String brand) {
        return score(detectedTitle, candidateTitle, brand) >= 0.28
                && hasCompatibleSpecs(detectedTitle, candidateTitle);
    }

    public static boolean isStrongMatch(String detectedTitle, String candidateTitle, String brand) {
        return score(detectedTitle, candidateTitle, brand) >= 0.38
                && hasCompatibleSpecs(detectedTitle, candidateTitle);
    }

    public static boolean hasCompatibleSpecs(String detectedTitle, String candidateTitle) {
        final Set<String> detectedQuantities = extractQuantityTokens(detectedTitle);
        if (detectedQuantities.isEmpty()) {
            return true;
        }

        final Set<String> candidateQuantities = extractQuantityTokens(candidateTitle);
        if (candidateQuantities.isEmpty()) {
            return true;
        }

        for (String detected : detectedQuantities) {
            if (candidateQuantities.contains(detected)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> extractQuantityTokens(String value) {
        final Set<String> tokens = new HashSet<>();
        final Matcher matcher = QUANTITY_PATTERN.matcher(normalize(value));
        while (matcher.find()) {
            tokens.add(matcher.group(1) + " " + matcher.group(2).toLowerCase(Locale.ROOT));
        }
        return tokens;
    }

    private static Set<String> tokenize(String value) {
        return Arrays.stream(normalize(value).split("\\s+"))
                .filter(token -> token.length() >= 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }
}
