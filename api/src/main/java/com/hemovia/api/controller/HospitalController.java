package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.HospitalRequestDTO;
import com.hemovia.api.model.DTOs.HospitalResponseDTO;
import com.hemovia.api.service.HospitalService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
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

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    @Operation(operationId = "createHospital", summary = "Cadastrar hospital")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hospital cadastrado", content = @Content(schema = @Schema(implementation = HospitalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<HospitalResponseDTO> createHospital(@Valid @RequestBody HospitalRequestDTO hospitalRequestDTO) {
        HospitalResponseDTO response = hospitalService.create(hospitalRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
