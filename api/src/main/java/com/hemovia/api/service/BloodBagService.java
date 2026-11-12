package com.hemovia.api.service;

import com.hemovia.api.dto.request.BloodBagRequestDTO;
import com.hemovia.api.dto.response.BloodBagResponseDTO;

import java.util.List;

public interface BloodBagService {

    List<BloodBagResponseDTO> getAllBloodBags();

    BloodBagResponseDTO getBloodBagById(String bloodBagId);

    BloodBagResponseDTO create (BloodBagRequestDTO bloodBagRequestDTO);

    BloodBagResponseDTO update(String bloodBagId, BloodBagRequestDTO bloodBagRequestDTO);

    void delete(String bloodBagId);

}
