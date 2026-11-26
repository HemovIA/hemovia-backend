package com.hemovia.api.dto.internal;
import com.hemovia.api.model.enums.TransportStatus;
import java.util.UUID;
public record TransportQuery(UUID id, TransportStatus status, UUID requestId, UUID vehicleId) { }
