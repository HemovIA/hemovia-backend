package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.dto.request.BloodCenterRequest;
import com.hemovia.api.application.dto.response.BloodCenterResponse;
import com.hemovia.api.application.usecase.bloodcenter.ManageBloodCenters;
import com.hemovia.api.infrastructure.configuration.ApiPrefixConfiguration;
import com.hemovia.api.presentation.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blood-centers")
@Tag(name = "Hemocentros")
@SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Papel ou token CSRF inválido", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
public class BloodCenterController {
    private final ManageBloodCenters centers;

    public BloodCenterController(ManageBloodCenters centers) {
        this.centers = centers;
    }

    @PostMapping
    @Operation(operationId = "createBloodCenter", summary = "Cadastrar hemocentro (ADMIN)",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponse(responseCode = "201", description = "Hemocentro cadastrado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Dados do cadastro inválidos", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<BloodCenterResponse> create(@Valid @RequestBody BloodCenterRequest request) {
        var center = centers.create(request);
        var location = URI.create(ApiPrefixConfiguration.PREFIXO + "/blood-centers/" + center.id());
        return ResponseEntity.created(location).body(center);
    }

    @GetMapping
    @Operation(operationId = "listBloodCenters", summary = "Listar hemocentros ativos")
    @ApiResponse(responseCode = "200", description = "Hemocentros ativos", useReturnTypeSchema = true)
    public List<BloodCenterResponse> list() {
        return centers.list();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getBloodCenter", summary = "Consultar hemocentro ativo")
    @ApiResponse(responseCode = "200", description = "Hemocentro encontrado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Hemocentro inexistente ou excluído", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public BloodCenterResponse get(@PathVariable UUID id) {
        return centers.get(id);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateBloodCenter", summary = "Atualizar hemocentro (ADMIN)",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponse(responseCode = "200", description = "Hemocentro atualizado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Identificador ou dados do cadastro inválidos", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Hemocentro inexistente ou excluído", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public BloodCenterResponse update(@PathVariable UUID id, @Valid @RequestBody BloodCenterRequest request) {
        return centers.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteBloodCenter", summary = "Excluir hemocentro logicamente (ADMIN)",
            description = "Oculta o cadastro e preserva os vínculos históricos.",
            parameters = @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Token CSRF obtido em /auth/csrf"))
    @ApiResponse(responseCode = "204", description = "Hemocentro excluído")
    @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Hemocentro inexistente ou excluído", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        centers.delete(id);
        return ResponseEntity.noContent().build();
    }
}
