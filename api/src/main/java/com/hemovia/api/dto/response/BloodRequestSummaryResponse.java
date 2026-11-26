package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.*;
import java.time.Instant;
import java.util.UUID;

public record BloodRequestSummaryResponse(UUID id, UUID hospitalId, String hospitalName, BloodType bloodType,
        RhFactor rhFactor, BloodComponent component, int quantity, RequestPriority priority,
        Instant deliveryDeadline, RequestStatus status, Instant createdAt, Instant updatedAt) { }
