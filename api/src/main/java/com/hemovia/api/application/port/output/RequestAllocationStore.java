package com.hemovia.api.application.port.output;

import com.hemovia.api.model.Allocation;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.BloodRequest;
import com.hemovia.api.model.enums.BloodComponent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestAllocationStore {
    Optional<BloodRequest> lockRequest(UUID requestId);
    List<BloodBag> lockEligibleBags(UUID centerId, BloodComponent component, LocalDate today);
    Allocation reserve(BloodRequest request, UUID centerId, List<BloodBag> bags);
}
