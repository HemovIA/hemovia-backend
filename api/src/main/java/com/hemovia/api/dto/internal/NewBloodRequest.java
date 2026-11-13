package com.hemovia.api.dto.internal;

import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RequestPriority;
import com.hemovia.api.model.enums.RhFactor;

import java.time.Instant;
import java.util.UUID;

public record NewBloodRequest(UUID hospitalId, BloodType bloodType, RhFactor rhFactor,
                              BloodComponent component, int quantity, RequestPriority priority,
                              Instant deliveryDeadline) { }
