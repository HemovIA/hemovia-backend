package com.hemovia.api.controller;

import com.hemovia.api.dto.request.BloodBagRequest;
import com.hemovia.api.dto.response.BloodBagResponse;
import com.hemovia.api.service.usecase.inventory.ManageBloodBags;
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

    private final ManageBloodBags bloodBagService;

    public BloodBagController(ManageBloodBags bloodBagService) {
        this.bloodBagService = bloodBagService;
    }

    @GetMapping
    @Operation(operationId = "listBloodBags", summary = "Listar bolsas de sangue")
    @ApiResponse(responseCode = "200", description = "Bolsas encontradas", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BloodBagResponse.class))))
    public ResponseEntity<List<BloodBagResponse>> getAllBloodBags() {
        return ResponseEntity.ok(bloodBagService.list());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getBloodBagById", summary = "Buscar bolsa por identificador")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Bolsa encontrada", content = @Content(schema = @Schema(implementation = BloodBagResponse.class))), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Bolsa não encontrada")})
    public ResponseEntity<BloodBagResponse> getBloodBagById(@PathVariable String id) {
        return ResponseEntity.ok(bloodBagService.get(id));
    }

    @PostMapping
    @Operation(operationId = "createBloodBag", summary = "Cadastrar bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Bolsa cadastrada", content = @Content(schema = @Schema(implementation = BloodBagResponse.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Vínculo informado não encontrado")})
    public ResponseEntity<BloodBagResponse> createBloodBag(@Valid @RequestBody BloodBagRequest request) {
        BloodBagResponse response = bloodBagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateBloodBag", summary = "Atualizar bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Bolsa atualizada", content = @Content(schema = @Schema(implementation = BloodBagResponse.class))), @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"), @ApiResponse(responseCode = "404", description = "Bolsa ou vínculo não encontrado")})
    public ResponseEntity<BloodBagResponse> updateBloodBag(@PathVariable String id,
                                                           @Valid @RequestBody BloodBagRequest request) {
        return ResponseEntity.ok(bloodBagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteBloodBag", summary = "Excluir bolsa de sangue")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Bolsa excluída"), @ApiResponse(responseCode = "400", description = "Identificador inválido"), @ApiResponse(responseCode = "404", description = "Bolsa não encontrada")})
    public ResponseEntity<Void> deleteBloodBag(@PathVariable String id) {
        bloodBagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
