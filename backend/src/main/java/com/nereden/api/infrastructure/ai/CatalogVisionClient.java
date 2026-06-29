package com.nereden.api.infrastructure.ai;

import com.nereden.api.domain.entity.ProductCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class CatalogVisionClient implements VisionAiClient {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public VisionDetectionResult analyze(String imageUrl) {
        log.warn(
                "Gemini/OpenAI yapılandırılmadı — tahmini analiz kullanılıyor. "
                        + "Gerçek tanıma için backend .env dosyasına GEMINI_API_KEY ekleyin. image={}",
                imageUrl
        );

        return new VisionDetectionResult(
                "Görselden tespit edilen ürün",
                "AI anahtarı olmadan oluşturulan tahmini sonuç. Daha doğru eşleşme için GEMINI_API_KEY yapılandırın.",
                null,
                ProductCategory.OTHER,
                new BigDecimal("499"),
                new BigDecimal("1999"),
                0.42,
                List.of("ürün", "alışveriş"),
                List.of("Benzer ürün alternatifi", "Ekonomik seçenek")
        );
    }
}
