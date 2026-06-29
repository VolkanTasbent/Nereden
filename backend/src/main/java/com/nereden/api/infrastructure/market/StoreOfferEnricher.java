package com.nereden.api.infrastructure.market;

import com.nereden.api.application.dto.market.MarketOffer;
import com.nereden.api.infrastructure.ai.VisionDetectionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StoreOfferEnricher {

    private static final List<StoreTarget> STORES = List.of(
            new StoreTarget("Trendyol", "trendyol.com"),
            new StoreTarget("Hepsiburada", "hepsiburada.com"),
            new StoreTarget("Amazon TR", "amazon.com.tr"),
            new StoreTarget("N11", "n11.com")
    );

    private final SearchApiGoogleLinkFinder searchApiGoogleLinkFinder;
    private final ProductPagePriceFetcher productPagePriceFetcher;

    public MarketOffer enrich(MarketOffer offer, List<MarketOffer> shoppingOffers, VisionDetectionResult detection) {
        if (offer.price() != null) {
            return offer;
        }

        final Optional<BigDecimal> shoppingPrice = findShoppingPrice(offer, shoppingOffers, detection);
        if (shoppingPrice.isPresent()) {
            return copyWithPrice(offer, shoppingPrice.get());
        }

        final Optional<BigDecimal> dedicatedShoppingPrice =
                searchApiGoogleLinkFinder.findShoppingPriceForProduct(offer, detection);
        if (dedicatedShoppingPrice.isPresent()) {
            return copyWithPrice(offer, dedicatedShoppingPrice.get());
        }

        final BigDecimal pagePrice = productPagePriceFetcher.fetchPrice(offer.url());
        if (pagePrice != null) {
            return copyWithPrice(offer, pagePrice);
        }

        return offer;
    }

    private Optional<BigDecimal> findShoppingPrice(
            MarketOffer offer,
            List<MarketOffer> shoppingOffers,
            VisionDetectionResult detection
    ) {
        return shoppingOffers.stream()
                .filter(shopping -> shopping.price() != null)
                .filter(shopping -> matchesStore(offer.storeName(), shopping))
                .filter(shopping -> ProductUrlMatcher.urlsLikelySameProduct(offer.url(), shopping.url())
                        || ProductTitleMatcher.isGoodMatch(
                                detection.title(),
                                shopping.productTitle(),
                                detection.brand()))
                .max(Comparator.comparingDouble(shopping -> ProductTitleMatcher.score(
                        detection.title(),
                        shopping.productTitle(),
                        detection.brand())))
                .map(MarketOffer::price);
    }

    private boolean matchesStore(String storeName, MarketOffer shopping) {
        final StoreTarget target = STORES.stream()
                .filter(store -> store.name().equals(storeName))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return false;
        }

        final String link = shopping.url().toLowerCase(Locale.ROOT);
        final String seller = shopping.storeName().toLowerCase(Locale.ROOT);
        final String storeToken = target.name().toLowerCase(Locale.ROOT).split(" ")[0];

        return link.contains(target.domain()) || seller.contains(storeToken);
    }

    private MarketOffer copyWithPrice(MarketOffer offer, BigDecimal price) {
        return new MarketOffer(
                offer.storeName(),
                offer.productTitle(),
                offer.url(),
                price,
                offer.directProductLink()
        );
    }
}
