package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record RouteNodeResponse(
        UUID id,
        String name,
        @Schema(description = "Latitude em graus", minimum = "-90", maximum = "90") double latitude,
        @Schema(description = "Longitude em graus", minimum = "-180", maximum = "180") double longitude,
        UUID bloodCenterId,
        UUID hospitalId) {
}
