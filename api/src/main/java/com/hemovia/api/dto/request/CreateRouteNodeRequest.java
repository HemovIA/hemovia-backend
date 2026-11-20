package com.hemovia.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Ponto vinculado a um hemocentro ou hospital, ou cruzamento com coordenadas próprias")
public record CreateRouteNodeRequest(
        @NotBlank @Size(max = 255) String name,
        @Schema(description = "Latitude em graus; exigida para cruzamento, omitida para vínculo", minimum = "-90", maximum = "90")
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @Schema(description = "Longitude em graus; exigida para cruzamento, omitida para vínculo", minimum = "-180", maximum = "180")
        @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @Schema(description = "Hemocentro vinculado, exclusivo em relação ao hospital") UUID bloodCenterId,
        @Schema(description = "Hospital vinculado, exclusivo em relação ao hemocentro") UUID hospitalId) {
}
