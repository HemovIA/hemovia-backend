package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Geometria GeoJSON com pares [longitude, latitude] em graus")
public record GeoJsonLineString(
        @Schema(allowableValues = "LineString", example = "LineString") String type,
        @Schema(description = "Pontos ordenados como [longitude, latitude]") List<List<Double>> coordinates) {
    public GeoJsonLineString {
        if (!"LineString".equals(type)) {
            throw new IllegalArgumentException("O tipo da geometria deve ser LineString.");
        }
        coordinates = coordinates.stream().map(List::copyOf).toList();
    }
}
