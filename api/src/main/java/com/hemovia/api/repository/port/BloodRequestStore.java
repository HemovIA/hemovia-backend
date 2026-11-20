package com.hemovia.api.repository.port;

import com.hemovia.api.dto.internal.NewBloodRequest;
import java.util.UUID;

public interface BloodRequestStore {
    UUID save(NewBloodRequest request);
}
