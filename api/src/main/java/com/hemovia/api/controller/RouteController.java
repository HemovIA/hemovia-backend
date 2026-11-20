package com.hemovia.api.controller;

import com.hemovia.api.dto.request.CalculateRouteRequest;
import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.dto.response.RoutePreviewResponse;
import com.hemovia.api.service.usecase.route.PreviewDeliveryRoute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routes")
public class RouteController {
    private final PreviewDeliveryRoute preview;

    public RouteController(PreviewDeliveryRoute preview) { this.preview = preview; }

    @Operation(summary = "Calcular prévia da rota para o hemocentro da sessão", description = "Restrito a HEMOCENTRO. Distância em quilômetros, duração em segundos e geometria em longitude/latitude.",
            security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prévia calculada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutePreviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Hemocentro ativo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "INVALID_ROUTE: rota inexistente ou fora da janela", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/calculate")
    public RoutePreviewResponse calculate(@Valid @RequestBody CalculateRouteRequest request) {
        return preview.execute(request);
    }
}
