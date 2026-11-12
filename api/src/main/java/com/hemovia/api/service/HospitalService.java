package com.hemovia.api.service;

import com.hemovia.api.dto.request.HospitalRequestDTO;
import com.hemovia.api.dto.response.HospitalResponseDTO;

public interface HospitalService {
    HospitalResponseDTO create(HospitalRequestDTO request);
}
