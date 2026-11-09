package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.service.BloodBagService;
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
@RequestMapping("/blood-bags")
@Tag(name = "Bolsas de sangue", description = "Consulta e manutenção de bolsas de sangue.")
public class BloodBagController {

    private final BloodBagService bloodBagService;

    public BloodBagController(BloodBagService bloodBagService) {
        this.bloodBagService = bloodBagService;
    }

    @GetMapping
    @Operation(operationId = "listBloodBags", summary = "Listar bolsas de sangue")
    @ApiResponse(responseCode = "200", description = "Bolsas encontradas", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BloodBagResponseDTO.class))))
    public ResponseEntity<List<BloodBagResponseDTO>> getAllBloodBags() {
        return ResponseEntity.ok(bloodBagService.getAllBloodBags());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getBloodBagById", summary = "Buscar bolsa por identificador")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Bolsa encontrada", content = @Content(schema = @Schema(implementation = BloodBagResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Bolsa não encontrada")})
    public ResponseEntity<BloodBagResponseDTO> getBloodBagById(@PathVariable String id) {
        return ResponseEntity.ok(bloodBagService.getBloodBagById(id));
    }

    @PostMapping
    @Operation(operationId = "createBloodBag", summary = "Cadastrar bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Bolsa cadastrada", content = @Content(schema = @Schema(implementation = BloodBagResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Vínculo informado não encontrado")})
    public ResponseEntity<BloodBagResponseDTO> createBloodBag(@Valid @RequestBody BloodBagRequestDTO bloodBagRequestDTO) {
        BloodBagResponseDTO response = bloodBagService.create(bloodBagRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateBloodBag", summary = "Atualizar bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Bolsa atualizada", content = @Content(schema = @Schema(implementation = BloodBagResponseDTO.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Bolsa ou vínculo não encontrado")})
    public ResponseEntity<BloodBagResponseDTO> updateBloodBag(@PathVariable String id,
                                                           @Valid @RequestBody BloodBagRequestDTO bloodBagRequestDTO) {
        return ResponseEntity.ok(bloodBagService.update(id, bloodBagRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteBloodBag", summary = "Excluir bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Bolsa excluída"), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Bolsa não encontrada")})
    public ResponseEntity<Void> deleteBloodBag(@PathVariable String id) {
        bloodBagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
