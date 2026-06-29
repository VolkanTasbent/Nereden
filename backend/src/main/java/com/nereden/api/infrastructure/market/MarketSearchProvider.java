package com.nereden.api.infrastructure.market;

import com.nereden.api.application.dto.market.MarketOffer;

import java.util.List;

public interface MarketSearchProvider {
    boolean isAvailable();
    List<MarketOffer> search(String query);
}
