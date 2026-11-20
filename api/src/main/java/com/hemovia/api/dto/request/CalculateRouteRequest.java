package com.hemovia.api.dto.request;

import com.hemovia.api.model.enums.BloodComponent;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CalculateRouteRequest(@NotNull UUID hospitalId, @NotNull BloodComponent component,
                                    @NotNull Instant deliveryDeadline) { }
