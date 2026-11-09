package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.HospitalRequestDTO;
import com.hemovia.api.model.DTOs.HospitalResponseDTO;
import com.hemovia.api.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospitals")

public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public ResponseEntity<HospitalResponseDTO> createHospital(@Valid @RequestBody HospitalRequestDTO hospitalRequestDTO) {
        HospitalResponseDTO response = hospitalService.create(hospitalRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
