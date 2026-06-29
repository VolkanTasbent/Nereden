package com.nereden.api.presentation.controller;

import com.nereden.api.application.dto.ApiResponse;
import com.nereden.api.application.dto.analysis.ProductSummaryResponse;
import com.nereden.api.application.dto.product.ProductDetailResponse;
import com.nereden.api.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/feed")
    public ApiResponse<List<ProductSummaryResponse>> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String category
    ) {
        final ProductService.FeedResult result = productService.getFeed(page, limit, category);
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .data(result.items())
                .meta(result.meta())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductSummaryResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String category
    ) {
        final ProductService.FeedResult result = productService.search(q, page, limit, category);
        return ApiResponse.<List<ProductSummaryResponse>>builder()
                .data(result.items())
                .meta(result.meta())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(productService.getProduct(id));
    }
}
