package com.hemovia.api.controller;

import com.hemovia.api.dto.request.VehicleRequest;
import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.dto.response.VehicleResponse;
import com.hemovia.api.service.usecase.vehicle.ManageVehicles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Veículos")
@SecurityRequirement(name = "cookieAuth")
public class VehicleController {
    private final ManageVehicles vehicles;

    public VehicleController(ManageVehicles vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @Operation(operationId = "createVehicle", summary = "Cadastrar veículo (ADMIN)",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo cadastrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel ou CSRF inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código já cadastrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicles.create(request));
    }

    @GetMapping
    @Operation(operationId = "listVehicles", summary = "Listar veículos", description = "Disponível a usuários autenticados com papel de negócio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículos cadastrados", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel sem acesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<VehicleResponse> list() {
        return vehicles.list();
    }
}
