package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.TransportStatus;
import java.time.Instant;
import java.util.UUID;

public record TransportResponse(UUID id, UUID allocationId, UUID requestId, UUID routeId, UUID vehicleId,
                                TransportStatus status, Instant startedAt, Instant completedAt) { }
