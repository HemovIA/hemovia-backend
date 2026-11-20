package com.hemovia.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartTransportRequest(@NotNull UUID allocationId, @NotNull UUID vehicleId) { }
