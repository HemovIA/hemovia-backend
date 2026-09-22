package com.hemovia.api.service;

import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;

import java.util.List;

public interface BloodBagService {

    List<BloodBagResponseDTO> getAllBloodBags();

    BloodBagResponseDTO getBloodBagById(String bloodBagId);

    BloodBagResponseDTO create (BloodBagRequestDTO bloodBagRequestDTO);

    BloodBagResponseDTO update(String bloodBagId, BloodBagRequestDTO bloodBagRequestDTO);

    void delete(String bloodBagId);

}
