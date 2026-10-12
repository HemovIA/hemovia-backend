package com.hemovia.api.service;

import com.hemovia.api.model.DTOs.HospitalRequestDTO;
import com.hemovia.api.model.DTOs.HospitalResponseDTO;

public interface HospitalService {
    HospitalResponseDTO create(HospitalRequestDTO request);
}
