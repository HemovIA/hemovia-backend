package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.dto.request.CreateBloodRequest;
import com.hemovia.api.application.dto.response.BloodRequestResponse;
import com.hemovia.api.application.port.input.CreateBloodRequestUseCase;
import com.hemovia.api.presentation.handler.ApiError;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
public class BloodRequestController {
    private final CreateBloodRequestUseCase create;

    public BloodRequestController(CreateBloodRequestUseCase create) { this.create = create; }

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
}
