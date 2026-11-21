package com.hemovia.api.dto.internal;

import com.hemovia.api.model.enums.*;
import java.util.UUID;

public record RequestQuery(UUID id, RequestStatus status, UUID hospitalId, BloodType bloodType,
                           RhFactor rhFactor, BloodComponent component, RequestPriority priority) { }
