package com.hemovia.api.application.dto.request;

import com.hemovia.api.domain.enums.RequestPriority;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RhFactor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record CreateBloodRequest(@NotNull BloodType bloodType, @NotNull RhFactor rhFactor,
        @NotNull BloodComponent component, @NotNull @Positive Integer quantity,
        @NotNull RequestPriority priority, @NotNull Instant deliveryDeadline) { }
