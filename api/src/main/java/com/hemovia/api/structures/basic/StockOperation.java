package com.hemovia.api.structures.basic;

import java.util.Objects;
import java.util.UUID;

public record StockOperation(UUID bagId, String previousStatus, String resultingStatus) {
    public StockOperation {
        Objects.requireNonNull(bagId, "bagId");
        Objects.requireNonNull(previousStatus, "previousStatus");
        Objects.requireNonNull(resultingStatus, "resultingStatus");
    }
}
