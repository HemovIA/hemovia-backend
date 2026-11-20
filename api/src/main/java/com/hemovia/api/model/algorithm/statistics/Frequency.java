package com.hemovia.api.model.algorithm.statistics;

public record Frequency(long value, long count) {
    public Frequency {
        if (value < 0 || count <= 0) {
            throw new IllegalArgumentException("Value must be non-negative and count must be positive");
        }
    }
}
