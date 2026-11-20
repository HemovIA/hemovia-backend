package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InventorySummaryResponse(
        @Schema(description = "Instante de geração em UTC") Instant generatedAt,
        @Schema(description = "Data de referência em UTC") LocalDate referenceDate,
        @Schema(description = "Total de bolsas elegíveis", minimum = "0", format = "int64") long totalBags,
        List<BloodTypeInventoryResponse> byBloodType,
        List<ComponentInventoryResponse> byComponent,
        List<InventoryCombinationResponse> byBloodTypeAndComponent) {
    public InventorySummaryResponse {
        byBloodType = List.copyOf(byBloodType);
        byComponent = List.copyOf(byComponent);
        byBloodTypeAndComponent = List.copyOf(byBloodTypeAndComponent);
    }
}
