package com.hemovia.api.controller;

import com.hemovia.api.dto.request.DonationRequest;
import com.hemovia.api.dto.response.DonationResponse;
import com.hemovia.api.service.usecase.inventory.ManageDonations;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
@Tag(name = "Doações", description = "Consulta e manutenção de doações.")
public class DonationController {

    private final ManageDonations donationService;

    public DonationController(ManageDonations donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    @Operation(operationId = "listDonations", summary = "Listar doações")
    @ApiResponse(responseCode = "200", description = "Doações encontradas", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationResponse.class))))
    public ResponseEntity<List<DonationResponse>> getAllDonations() {
        return ResponseEntity.ok(donationService.list());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDonationById", summary = "Buscar doação por identificador")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doação encontrada", content = @Content(schema = @Schema(implementation = DonationResponse.class))), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Doação não encontrada")})
    public ResponseEntity<DonationResponse> getDonationById(@PathVariable String id) {
        return ResponseEntity.ok(donationService.get(id));
    }

    @PostMapping
    @Operation(operationId = "createDonation", summary = "Registrar doação")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Doação registrada", content = @Content(schema = @Schema(implementation = DonationResponse.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Vínculo informado não encontrado")})
    public ResponseEntity<DonationResponse> createDonation(@Valid @RequestBody DonationRequest request) {
        DonationResponse response = donationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateDonation", summary = "Atualizar doação")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doação atualizada", content = @Content(schema = @Schema(implementation = DonationResponse.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Doação ou vínculo não encontrado")})
    public ResponseEntity<DonationResponse> updateDonation(@PathVariable String id,
                                                            @Valid @RequestBody DonationRequest request) {
        return ResponseEntity.ok(donationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteDonation", summary = "Excluir doação")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Doação excluída"), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Doação não encontrada")})
    public ResponseEntity<Void> deleteDonation(@PathVariable String id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
