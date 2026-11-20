package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RouteEdgeResponse(
        UUID id,
        UUID fromNodeId,
        UUID toNodeId,
        @Schema(description = "Distância em quilômetros") BigDecimal distanceKm,
        @Schema(description = "Duração estimada em segundos") long travelSeconds,
        GeoJsonLineString geometry,
        @Schema(description = "Provedor de rotas", example = "OSRM") String provider,
        @Schema(description = "Instante da consulta ao provedor") Instant calculatedAt) {
}
