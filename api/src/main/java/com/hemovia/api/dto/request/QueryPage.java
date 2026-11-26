package com.hemovia.api.dto.request;

public record QueryPage(int page, int size) {
    public QueryPage {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Página deve ser não negativa e tamanho deve estar entre 1 e 100.");
        }
    }

    public static QueryPage of(Integer page, Integer size) {
        return new QueryPage(page == null ? 0 : page, size == null ? 20 : size);
    }
}
