package com.hemovia.api.controller;
import com.hemovia.api.dto.request.DonorRequest;
import com.hemovia.api.dto.response.DonorResponse;
import com.hemovia.api.service.usecase.donor.ManageDonors;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/donors")
public class DonorController {
    private final ManageDonors donors;
    public DonorController(ManageDonors donors) { this.donors = donors; }
    @Operation(summary = "Buscar doador por CPF exato", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doador encontrado"), @ApiResponse(responseCode = "400", description = "CPF inválido"), @ApiResponse(responseCode = "401", description = "Sessão ausente"), @ApiResponse(responseCode = "403", description = "Acesso negado"), @ApiResponse(responseCode = "404", description = "Doador não encontrado")})
    @GetMapping public DonorResponse find(@RequestParam String cpf) { return donors.find(cpf); }
    @Operation(summary = "Cadastrar doador", security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Doador cadastrado"), @ApiResponse(responseCode = "400", description = "Dados inválidos"), @ApiResponse(responseCode = "401", description = "Sessão ausente"), @ApiResponse(responseCode = "403", description = "Acesso negado"), @ApiResponse(responseCode = "409", description = "CPF já cadastrado")})
    @PostMapping public ResponseEntity<DonorResponse> create(@Valid @RequestBody DonorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donors.create(request));
    }
}

