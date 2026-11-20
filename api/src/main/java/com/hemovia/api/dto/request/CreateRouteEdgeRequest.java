package com.hemovia.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Ligação direcional; distância e duração são obtidas do provedor de rotas")
public record CreateRouteEdgeRequest(@NotNull UUID fromNodeId, @NotNull UUID toNodeId) {
}
