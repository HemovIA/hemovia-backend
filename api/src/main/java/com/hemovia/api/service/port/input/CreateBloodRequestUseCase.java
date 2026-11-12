package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.request.CreateBloodRequest;
import com.hemovia.api.dto.response.BloodRequestResponse;

public interface CreateBloodRequestUseCase {
    BloodRequestResponse execute(CreateBloodRequest request);
}
