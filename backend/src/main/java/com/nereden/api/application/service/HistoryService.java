package com.nereden.api.application.service;

import com.nereden.api.application.dto.history.SearchHistoryResponse;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.SearchHistory;
import com.nereden.api.domain.entity.User;
import com.nereden.api.domain.repository.SearchHistoryRepository;
import com.nereden.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public List<SearchHistoryResponse> getHistory(UUID userId) {
        return searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void saveImageSearch(UUID userId, String imageUrl, Product product) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        searchHistoryRepository.save(SearchHistory.builder()
                .user(user)
                .imageUrl(imageUrl)
                .product(product)
                .build());
    }

    @Transactional
    public void saveTextSearch(UUID userId, String query) {
        if (query == null || query.isBlank()) return;

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        searchHistoryRepository.save(SearchHistory.builder()
                .user(user)
                .query(query.trim())
                .build());
    }

    @Transactional
    public void clearHistory(UUID userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteItem(UUID userId, UUID historyId) {
        final SearchHistory item = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("Geçmiş kaydı bulunamadı."));

        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bu kayda erişim yetkiniz yok.");
        }

        searchHistoryRepository.delete(item);
    }

    private SearchHistoryResponse toResponse(SearchHistory item) {
        return SearchHistoryResponse.builder()
                .id(item.getId().toString())
                .query(item.getQuery())
                .imageUrl(item.getImageUrl())
                .productId(item.getProduct() != null ? item.getProduct().getId().toString() : null)
                .productTitle(item.getProduct() != null ? item.getProduct().getTitle() : null)
                .createdAt(item.getCreatedAt().toString())
                .build();
    }
}
