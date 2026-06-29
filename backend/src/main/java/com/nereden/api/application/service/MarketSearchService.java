package com.nereden.api.application.service;

import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import com.nereden.api.infrastructure.market.ExactProductLinkResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSearchService {

    private final ExactProductLinkResolver exactProductLinkResolver;

    public List<MarketOffer> search(VisionDetectionResult detection, String imageUrl) {
        final List<MarketOffer> offers = exactProductLinkResolver.resolve(detection, imageUrl);
        final long directCount = offers.stream().filter(MarketOffer::directProductLink).count();
        log.info("Resolved {} store links ({} direct product pages)", offers.size(), directCount);
        return sortByPrice(offers);
    }

    private List<MarketOffer> sortByPrice(List<MarketOffer> offers) {
        return offers.stream()
                .sorted(Comparator.comparing(MarketOffer::price, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}
