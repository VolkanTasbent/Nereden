package com.nereden.api.infrastructure.market;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TurkishPriceParser {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d[\\d.,]*");

    private TurkishPriceParser() {}

    public static BigDecimal fromJsonNode(JsonNode item) {
        if (item != null && item.has("extracted_price") && !item.path("extracted_price").isNull()) {
            final double value = item.path("extracted_price").asDouble(0);
            if (value > 0) {
                return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
            }
        }
        return fromText(item != null ? item.path("price").asText("") : "");
    }

    public static BigDecimal fromText(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        final Matcher matcher = NUMBER_PATTERN.matcher(raw.replace(" ", ""));
        if (!matcher.find()) {
            return null;
        }

        return fromNumericToken(matcher.group());
    }

    private static BigDecimal fromNumericToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String cleaned = token.trim();
        final int commaIndex = cleaned.lastIndexOf(',');
        final int dotIndex = cleaned.lastIndexOf('.');

        if (commaIndex >= 0 && dotIndex >= 0) {
            if (commaIndex > dotIndex) {
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (commaIndex >= 0) {
            final String afterComma = cleaned.substring(commaIndex + 1);
            if (afterComma.length() <= 2) {
                cleaned = cleaned.replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (dotIndex >= 0) {
            final String afterDot = cleaned.substring(dotIndex + 1);
            if (afterDot.length() == 3) {
                cleaned = cleaned.replace(".", "");
            }
        }

        try {
            final BigDecimal value = new BigDecimal(cleaned);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return value.setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
