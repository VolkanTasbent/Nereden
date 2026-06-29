package com.nereden.api.application.service;

import com.nereden.api.application.dto.favorite.FavoriteResponse;
import com.nereden.api.application.mapper.ProductMapper;
import com.nereden.api.domain.entity.Favorite;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.User;
import com.nereden.api.domain.repository.FavoriteRepository;
import com.nereden.api.domain.repository.ProductRepository;
import com.nereden.api.domain.repository.StoreRepository;
import com.nereden.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public List<FavoriteResponse> getFavorites(UUID userId) {
        return favoriteRepository.findByUserIdWithProduct(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean isFavorite(UUID userId, UUID productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public FavoriteResponse addFavorite(UUID userId, UUID productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return favoriteRepository.findByUserIdAndProductId(userId, productId)
                    .map(this::toResponse)
                    .orElseThrow();
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));
        final Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı."));

        final Favorite saved = favoriteRepository.save(Favorite.builder()
                .user(user)
                .product(product)
                .build());

        return toResponse(saved);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        final Product product = favorite.getProduct();
        return FavoriteResponse.builder()
                .id(favorite.getId().toString())
                .productId(product.getId().toString())
                .product(ProductMapper.toSummary(
                        product,
                        storeRepository.findByProductId(product.getId()),
                        false,
                        null))
                .createdAt(favorite.getCreatedAt().toString())
                .build();
    }
}
