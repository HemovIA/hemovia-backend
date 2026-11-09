package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.request.CreateBloodRequest;
import com.hemovia.api.application.dto.response.BloodRequestResponse;

public interface CreateBloodRequestUseCase {
    BloodRequestResponse execute(CreateBloodRequest request);
}
