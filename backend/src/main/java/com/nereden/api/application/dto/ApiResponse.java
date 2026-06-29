package com.nereden.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private T data;
    private ApiMeta meta;

    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder().data(data).build();
    }
}
