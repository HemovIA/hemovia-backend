package com.hemovia.api.controller;

import com.hemovia.api.dto.request.HospitalRequest;
import com.hemovia.api.dto.response.HospitalResponse;
import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.service.usecase.hospital.CreateHospital;
import jakarta.validation.Valid;
import java.util.UUID;
import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.PageResponse;
import com.hemovia.api.service.usecase.hospital.QueryHospitals;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitals")
@Tag(name = "Hospitais", description = "Cadastro de hospitais parceiros.")
public class HospitalController {

    private final CreateHospital createHospital;
    private final QueryHospitals query;

    public HospitalController(CreateHospital createHospital, QueryHospitals query) {
        this.createHospital = createHospital;
        this.query = query;
    }

    @PostMapping
    @Operation(operationId = "createHospital", summary = "Cadastrar hospital")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hospital cadastrado", content = @Content(schema = @Schema(implementation = HospitalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody HospitalRequest request) {
        HospitalResponse response = createHospital.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    @Operation(summary = "Listar hospitais", security = @SecurityRequirement(name = "cookieAuth"))
    public PageResponse<HospitalResponse> list(@RequestParam(required=false) String name, @RequestParam(required=false) String city,
            @Parameter(description = "Página a partir de zero", schema = @Schema(defaultValue = "0", minimum = "0")) @RequestParam(required=false) Integer page, @Parameter(description = "Itens por página", schema = @Schema(defaultValue = "20", minimum = "1", maximum = "100")) @RequestParam(required=false) Integer size) {
        return query.list(name,city,QueryPage.of(page,size));
    }
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta realizada"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @Operation(summary = "Detalhar hospital", security = @SecurityRequirement(name = "cookieAuth"))
    public HospitalResponse detail(@PathVariable UUID id) { return query.detail(id); }
}
