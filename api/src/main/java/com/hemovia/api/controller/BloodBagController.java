package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.service.BloodBagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blood-bags")
public class BloodBagController {

    private final BloodBagService bloodBagService;

    public BloodBagController(BloodBagService bloodBagService) {
        this.bloodBagService = bloodBagService;
    }

    @GetMapping
    public ResponseEntity<List<BloodBagResponseDTO>> getAllBloodBags() {
        return ResponseEntity.ok(bloodBagService.getAllBloodBags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodBagResponseDTO> getBloodBagById(@PathVariable String id) {
        return ResponseEntity.ok(bloodBagService.getBloodBagById(id));
    }

    @PostMapping
    public ResponseEntity<BloodBagResponseDTO> createBloodBag(@Valid @RequestBody BloodBagRequestDTO bloodBagRequestDTO) {
        BloodBagResponseDTO response = bloodBagService.create(bloodBagRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BloodBagResponseDTO> updateBloodBag(@PathVariable String id,
                                                           @Valid @RequestBody BloodBagRequestDTO bloodBagRequestDTO) {
        return ResponseEntity.ok(bloodBagService.update(id, bloodBagRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBloodBag(@PathVariable String id) {
        bloodBagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
