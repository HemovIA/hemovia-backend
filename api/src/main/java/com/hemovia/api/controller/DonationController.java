package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.DonationRequestDTO;
import com.hemovia.api.model.DTOs.DonationResponseDTO;
import com.hemovia.api.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    public ResponseEntity<List<DonationResponseDTO>> getAllDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponseDTO> getDonationById(@PathVariable String id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping
    public ResponseEntity<DonationResponseDTO> createDonation(@Valid @RequestBody DonationRequestDTO donationRequestDTO) {
        DonationResponseDTO response = donationService.create(donationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationResponseDTO> updateDonation(@PathVariable String id,
                                                            @Valid @RequestBody DonationRequestDTO donationRequestDTO) {
        return ResponseEntity.ok(donationService.update(id, donationRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable String id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
