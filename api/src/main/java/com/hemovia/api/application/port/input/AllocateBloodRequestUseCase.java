package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.response.AllocationResponse;
import java.util.UUID;

public interface AllocateBloodRequestUseCase {
    AllocationResponse execute(UUID requestId);
}
