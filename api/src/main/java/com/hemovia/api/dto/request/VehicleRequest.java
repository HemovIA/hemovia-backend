package com.hemovia.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank @Size(max = 255) @Schema(description = "Código único do veículo", maxLength = 255) String code,
        @NotNull @Positive @Schema(description = "Capacidade em bolsas", minimum = "1") Integer capacity
) { }
