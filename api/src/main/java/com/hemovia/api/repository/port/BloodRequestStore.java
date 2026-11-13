package com.hemovia.api.repository.port;

import com.hemovia.api.dto.request.CreateBloodRequest;
import java.util.UUID;

public interface BloodRequestStore {
    UUID save(UUID hospitalId, CreateBloodRequest request);
}
