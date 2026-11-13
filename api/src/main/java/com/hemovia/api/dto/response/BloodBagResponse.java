package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "BloodBagResponse", description = "Bolsa de sangue retornada pela API.")
public record BloodBagResponse(
        @Schema(format = "uuid", example = "42f0c6fb-5c5a-4e46-b2cc-7df77d523a01")
        UUID bloodBagId,

        @Schema(example = "O")
        String bloodType,

        @Schema(example = "POSITIVE")
        String rhFactor,

        @Schema(format = "date", example = "2030-12-31")
        String expirationDate,

        @Schema(example = "WHOLE_BLOOD")
        String component,

        @Schema(format = "date", example = "2026-10-22")
        String collectionDate,

        @Schema(example = "AVAILABLE")
        String status,

        @Schema(format = "uuid", nullable = true)
        UUID donorId,

        @Schema(format = "uuid", nullable = true)
        UUID bloodCenterId
) {
}
