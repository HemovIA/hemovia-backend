package com.hemovia.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "HospitalRequest", description = "Dados de entrada do hospital. Latitude e longitude são coordenadas geográficas; prioridade vai de 1 a 5.")
public record HospitalRequest(
        @NotBlank
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hospital Municipal")
        String name,

        @NotBlank
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "São Paulo")
        String city,

        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-90", maximum = "90", description = "Latitude em graus", example = "-23.5505")
        Double latitude,

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-180", maximum = "180", description = "Longitude em graus", example = "-46.6333")
        Double longitude,

        @NotNull
        @Min(1)
        @Max(5)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5", description = "Prioridade operacional", example = "3")
        Integer priority
) {
}
