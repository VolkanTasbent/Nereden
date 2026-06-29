package com.nereden.api.application.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSummaryResponse {
    private String id;
    private String name;
    private String url;
    private String logoUrl;
    private BigDecimal price;
    private String currency;
    private boolean inStock;
}
