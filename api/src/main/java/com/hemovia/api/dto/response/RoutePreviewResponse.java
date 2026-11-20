package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RoutePreviewResponse(List<UUID> nodeIds,
        @Schema(description = "Distância em quilômetros") BigDecimal distanceKm,
        @Schema(description = "Duração estimada em segundos") long estimatedDurationSeconds,
        GeoJsonLineString geometry) {
    public RoutePreviewResponse { nodeIds = List.copyOf(nodeIds); }
}
