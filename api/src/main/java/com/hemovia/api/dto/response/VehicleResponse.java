package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        @Schema(description = "Código único do veículo", maxLength = 255) String code,
        @Schema(description = "Capacidade em bolsas", minimum = "1") Integer capacity
) { }
