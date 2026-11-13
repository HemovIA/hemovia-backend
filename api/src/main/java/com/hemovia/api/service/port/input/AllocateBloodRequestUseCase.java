package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.response.AllocationResponse;
import java.util.UUID;

public interface AllocateBloodRequestUseCase {
    AllocationResponse execute(UUID requestId);
}
