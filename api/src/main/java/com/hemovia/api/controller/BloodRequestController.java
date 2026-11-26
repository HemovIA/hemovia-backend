package com.hemovia.api.controller;

import com.hemovia.api.dto.request.CreateBloodRequest;
import com.hemovia.api.dto.response.AllocationResponse;
import com.hemovia.api.dto.response.BloodRequestResponse;
import com.hemovia.api.service.usecase.request.AllocateBloodRequest;
import com.hemovia.api.service.usecase.request.CancelBloodRequest;
import com.hemovia.api.service.usecase.request.CreateHospitalBloodRequest;
import com.hemovia.api.dto.response.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import com.hemovia.api.dto.internal.RequestQuery;
import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.PageResponse;
import com.hemovia.api.dto.response.BloodRequestSummaryResponse;
import com.hemovia.api.dto.response.BloodRequestDetailsResponse;
import com.hemovia.api.model.enums.*;
import com.hemovia.api.service.usecase.request.QueryBloodRequests;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/requests")
public class BloodRequestController {
    private final CreateHospitalBloodRequest create;
    private final AllocateBloodRequest allocate;
    private final CancelBloodRequest cancel;
    private final QueryBloodRequests query;

    public BloodRequestController(CreateHospitalBloodRequest create, AllocateBloodRequest allocate, CancelBloodRequest cancel, QueryBloodRequests query) {
        this.create = create;
        this.allocate = allocate;
        this.cancel = cancel;
        this.query = query;
    }

    @Operation(summary = "Emitir requisição hospitalar", security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Requisição criada", content = @Content(schema = @Schema(implementation = BloodRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<BloodRequestResponse> create(@Valid @RequestBody CreateBloodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(request));
    }

    @Operation(summary = "Alocar hemácias para requisição", description = "Restrito a HEMOCENTRO. A rota contém distância em quilômetros, duração em segundos e geometria longitude/latitude.", security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alocação criada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AllocationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Requisição ou hemocentro ativo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Requisição processada ou estoque insuficiente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Componente não elegível ou INVALID_ROUTE: rota inexistente ou fora da janela", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/allocate")
    public ResponseEntity<AllocationResponse> allocate(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allocate.execute(id));
    }

    @Operation(summary = "Cancelar requisição hospitalar", security = @SecurityRequirement(name = "cookieAuth"),
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf", schema = @Schema(type = "string")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requisição cancelada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BloodRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Requisição não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Estado não permite cancelamento", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BloodRequestResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(cancel.execute(id));
    }
    @Operation(summary = "Listar requisições", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PageResponse<BloodRequestSummaryResponse> list(@RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) UUID hospitalId, @RequestParam(required = false) BloodType bloodType,
            @RequestParam(required = false) RhFactor rhFactor, @RequestParam(required = false) BloodComponent component,
            @RequestParam(required = false) RequestPriority priority, @Parameter(description = "Página a partir de zero", schema = @Schema(defaultValue = "0", minimum = "0")) @RequestParam(required = false) Integer page,
            @Parameter(description = "Itens por página", schema = @Schema(defaultValue = "20", minimum = "1", maximum = "100")) @RequestParam(required = false) Integer size) {
        return query.list(new RequestQuery(null, status, hospitalId, bloodType, rhFactor, component, priority), QueryPage.of(page, size));
    }

    @Operation(summary = "Detalhar requisição", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public BloodRequestDetailsResponse detail(@PathVariable UUID id) { return query.detail(id); }
}

