package com.nereden.api.application.service;

import com.nereden.api.application.dto.analysis.AnalysisRequestResponse;
import com.nereden.api.application.dto.analysis.AnalysisResultResponse;
import com.nereden.api.application.dto.analysis.PriceRangeResponse;
import com.nereden.api.application.dto.analysis.ProductMatchResponse;
import com.nereden.api.application.dto.analysis.ProductSummaryResponse;
import com.nereden.api.application.mapper.ProductMapper;
import com.nereden.api.domain.entity.AnalysisRequest;
import com.nereden.api.domain.entity.AnalysisStatus;
import com.nereden.api.domain.entity.Product;
import com.nereden.api.domain.entity.Store;
import com.nereden.api.domain.entity.User;
import com.nereden.api.domain.repository.AnalysisRequestRepository;
import com.nereden.api.domain.repository.ProductRepository;
import com.nereden.api.domain.repository.StoreRepository;
import com.nereden.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final AnalysisProcessor analysisProcessor;

    @Transactional
    public AnalysisRequestResponse createRequest(UUID userId, String imageUrl) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        final AnalysisRequest request = AnalysisRequest.builder()
                .user(user)
                .imageUrl(imageUrl)
                .status(AnalysisStatus.PENDING)
                .build();

        final AnalysisRequest saved = analysisRequestRepository.save(request);
        runAfterCommit(() -> analysisProcessor.processAsync(saved.getId()));
        return toRequestResponse(saved);
    }

    @Transactional
    public AnalysisRequestResponse retry(UUID requestId, UUID userId) {
        final AnalysisRequest request = analysisRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Analiz bulunamadı."));

        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bu analize erişim yetkiniz yok.");
        }

        productRepository.findByAnalysisRequestId(requestId)
                .forEach(productRepository::delete);

        request.setStatus(AnalysisStatus.PENDING);
        request.setCompletedAt(null);
        request.setErrorMessage(null);
        request.setDetectedTitle(null);
        request.setConfidence(null);
        analysisRequestRepository.save(request);

        runAfterCommit(() -> analysisProcessor.processAsync(requestId));
        return toRequestResponse(request);
    }

    public AnalysisRequestResponse getRequest(UUID requestId) {
        final AnalysisRequest request = analysisRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Analiz bulunamadı."));
        return toRequestResponse(request);
    }

    public AnalysisResultResponse getResult(UUID requestId) {
        final AnalysisRequest request = analysisRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Analiz bulunamadı."));

        if (request.getStatus() == AnalysisStatus.FAILED) {
            throw new IllegalStateException(
                    request.getErrorMessage() != null ? request.getErrorMessage() : "Analiz başarısız oldu.");
        }

        if (request.getStatus() != AnalysisStatus.COMPLETED) {
            throw new IllegalStateException("Analiz henüz tamamlanmadı.");
        }

        final List<Product> products = productRepository.findByAnalysisRequestId(requestId);
        if (products.isEmpty()) {
            return buildEmptyResult(request);
        }

        final Product exact = products.get(0);
        final List<Store> exactStores = storeRepository.findByProductId(exact.getId());

        final List<ProductSummaryResponse> similar = products.stream()
                .skip(1)
                .map(p -> ProductMapper.toSummary(
                        p, storeRepository.findByProductId(p.getId()), false, 0.85))
                .toList();

        final List<ProductSummaryResponse> cheaper = similar.stream()
                .filter(p -> p.getPriceRange() != null
                        && exact.getMinPrice() != null
                        && p.getPriceRange().getMin().compareTo(exact.getMinPrice()) < 0)
                .sorted(Comparator.comparing(p -> p.getPriceRange().getMin()))
                .toList();

        return AnalysisResultResponse.builder()
                .id(UUID.randomUUID().toString())
                .requestId(request.getId().toString())
                .matches(ProductMatchResponse.builder()
                        .exactMatch(ProductMapper.toSummary(exact, exactStores, true,
                                request.getConfidence() != null ? request.getConfidence() : 0.9))
                        .similarProducts(similar)
                        .cheaperAlternatives(cheaper)
                        .estimatedPriceRange(ProductMapper.toPriceRange(exact))
                        .build())
                .confidence(request.getConfidence() != null ? request.getConfidence() : 0.85)
                .processingTimeMs(calculateProcessingMs(request))
                .createdAt(Instant.now().toString())
                .build();
    }

    private long calculateProcessingMs(AnalysisRequest request) {
        if (request.getCompletedAt() == null) {
            return 0;
        }
        return request.getCompletedAt().toEpochMilli() - request.getCreatedAt().toEpochMilli();
    }

    private AnalysisResultResponse buildEmptyResult(AnalysisRequest request) {
        return AnalysisResultResponse.builder()
                .id(UUID.randomUUID().toString())
                .requestId(request.getId().toString())
                .matches(ProductMatchResponse.builder()
                        .estimatedPriceRange(PriceRangeResponse.builder()
                                .min(new BigDecimal("500"))
                                .max(new BigDecimal("2000"))
                                .currency("TRY")
                                .build())
                        .build())
                .confidence(0.5)
                .processingTimeMs(calculateProcessingMs(request))
                .createdAt(Instant.now().toString())
                .build();
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private AnalysisRequestResponse toRequestResponse(AnalysisRequest request) {
        return AnalysisRequestResponse.builder()
                .id(request.getId().toString())
                .userId(request.getUser().getId().toString())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus().name().toLowerCase())
                .createdAt(request.getCreatedAt().toString())
                .completedAt(request.getCompletedAt() != null ? request.getCompletedAt().toString() : null)
                .errorMessage(request.getErrorMessage())
                .build();
    }
}
