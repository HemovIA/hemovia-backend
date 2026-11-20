package com.hemovia.api.dto.internal;

import com.hemovia.api.model.RoadSegment;
import java.time.Instant;
import java.util.UUID;

public record StoredRouteEdge(UUID id, UUID fromNodeId, UUID toNodeId, RoadSegment segment, String provider, Instant calculatedAt) {
}
