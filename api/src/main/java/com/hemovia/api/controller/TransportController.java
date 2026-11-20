package com.hemovia.api.controller;

import com.hemovia.api.dto.request.StartTransportRequest;
import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.dto.response.TransportResponse;
import com.hemovia.api.service.usecase.transport.CompleteTransport;
import com.hemovia.api.service.usecase.transport.StartTransport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transports")
@Tag(name = "Transportes")
@SecurityRequirement(name = "cookieAuth")
public class TransportController {
    private final StartTransport start;
    private final CompleteTransport complete;

    public TransportController(StartTransport start, CompleteTransport complete) {
        this.start = start;
        this.complete = complete;
    }

    @PostMapping("/start")
    @Operation(operationId = "startTransport", summary = "Iniciar transporte (HEMOCENTRO)",
            description = "Inicia a viagem da alocação do hemocentro usando a rota já reservada. GPS e temperatura não são acompanhados nesta operação.",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf", schema = @Schema(type = "string")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transporte iniciado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados ou identificadores inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Alocação, hemocentro ativo ou veículo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Veículo indisponível (VEHICLE_UNAVAILABLE)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Estado, capacidade ou janela incompatível", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TransportResponse> start(@Valid @RequestBody StartTransportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(start.execute(request));
    }

    @PostMapping("/{id}/complete")
    @Operation(operationId = "completeTransport", summary = "Confirmar entrega (HOSPITAL)",
            description = "O hospital de destino confirma o recebimento e encerra a viagem.",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf", schema = @Schema(type = "string")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrega confirmada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Transporte não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Estado ou lote incompatível", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TransportResponse> complete(
            @Parameter(description = "Identificador do transporte", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID id) {
        return ResponseEntity.ok(complete.execute(id));
    }
}
