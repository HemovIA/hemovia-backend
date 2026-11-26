package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BloodRequestDetailsResponse(BloodRequestSummaryResponse request, List<AllocationDetails> allocations) {
    public record AllocationDetails(UUID id, UUID bloodCenterId, AllocationStatus status, Instant createdAt,
            Instant updatedAt, List<BagDetails> bags, StoredRoute route, UUID transportId) { }
    public record BagDetails(UUID id, ReservationStatus status) { }
    public record StoredRoute(UUID id, BigDecimal distanceKm, long estimatedDurationSeconds,
                              List<UUID> nodeIds, List<List<Double>> coordinates) { }
}
