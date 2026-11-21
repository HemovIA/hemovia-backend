package com.hemovia.api.dto.response;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResponse<>(content, page, size, totalElements,
                (int) Math.min(Integer.MAX_VALUE, totalElements / size + (totalElements % size == 0 ? 0 : 1)));
    }
}
