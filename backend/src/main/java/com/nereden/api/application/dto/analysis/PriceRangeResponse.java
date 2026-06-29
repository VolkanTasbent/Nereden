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
public class PriceRangeResponse {
    private BigDecimal min;
    private BigDecimal max;
    private String currency;
}
