package com.hemovia.api.model;

import com.hemovia.api.model.valueobject.GeoPoint;
import java.math.BigDecimal;
import java.util.List;

public record RoadSegment(BigDecimal distanceKm, long durationSeconds, List<GeoPoint> geometry) {
    public RoadSegment {
        geometry = List.copyOf(geometry);
    }
}
