package com.hemovia.api.application.port.output;

import com.hemovia.api.application.dto.request.CreateBloodRequest;
import java.util.UUID;

public interface BloodRequestStore {
    UUID save(UUID hospitalId, CreateBloodRequest request);
}
