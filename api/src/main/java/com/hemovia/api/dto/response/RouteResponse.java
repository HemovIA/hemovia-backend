package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RouteResponse(UUID id, List<UUID> nodeIds,
        @Schema(description = "Distância em quilômetros") BigDecimal distanceKm,
        @Schema(description = "Duração estimada em segundos") long estimatedDurationSeconds,
        GeoJsonLineString geometry) {
    public RouteResponse { nodeIds = List.copyOf(nodeIds); }
}
