package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.DonationRequestDTO;
import com.hemovia.api.model.DTOs.DonationResponseDTO;
import com.hemovia.api.service.DonationService;
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

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    @Operation(operationId = "listDonations", summary = "Listar doações")
    @ApiResponse(responseCode = "200", description = "Doações encontradas", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DonationResponseDTO.class))))
    public ResponseEntity<List<DonationResponseDTO>> getAllDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDonationById", summary = "Buscar doação por identificador")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doação encontrada", content = @Content(schema = @Schema(implementation = DonationResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Doação não encontrada")})
    public ResponseEntity<DonationResponseDTO> getDonationById(@PathVariable String id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping
    @Operation(operationId = "createDonation", summary = "Registrar doação")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Doação registrada", content = @Content(schema = @Schema(implementation = DonationResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Vínculo informado não encontrado")})
    public ResponseEntity<DonationResponseDTO> createDonation(@Valid @RequestBody DonationRequestDTO donationRequestDTO) {
        DonationResponseDTO response = donationService.create(donationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateDonation", summary = "Atualizar doação")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Doação atualizada", content = @Content(schema = @Schema(implementation = DonationResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Doação ou vínculo não encontrado")})
    public ResponseEntity<DonationResponseDTO> updateDonation(@PathVariable String id,
                                                            @Valid @RequestBody DonationRequestDTO donationRequestDTO) {
        return ResponseEntity.ok(donationService.update(id, donationRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteDonation", summary = "Excluir doação")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Doação excluída"), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Doação não encontrada")})
    public ResponseEntity<Void> deleteDonation(@PathVariable String id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
