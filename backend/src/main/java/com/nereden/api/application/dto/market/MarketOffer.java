package com.nereden.api.application.dto.market;

import java.math.BigDecimal;

public record MarketOffer(
        String storeName,
        String productTitle,
        String url,
        BigDecimal price,
        boolean directProductLink
) {
    public MarketOffer(String storeName, String productTitle, String url, BigDecimal price) {
        this(storeName, productTitle, url, price, false);
    }
}

