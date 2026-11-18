package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "HospitalResponse", description = "Hospital cadastrado.")
public record HospitalResponse(
        @Schema(format = "uuid", example = "0c5c4aaf-546c-4b7d-9b4b-41a3e481a903")
        UUID id,

        @Schema(example = "Hospital Municipal")
        String name,

        @Schema(example = "São Paulo")
        String city,

        @Schema(description = "Latitude em graus", example = "-23.5505")
        double latitude,

        @Schema(description = "Longitude em graus", example = "-46.6333")
        double longitude,

        @Schema(description = "Prioridade operacional", example = "3")
        int priority
) {
}
