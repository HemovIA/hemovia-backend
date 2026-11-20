package com.hemovia.api.repository.port;

import com.hemovia.api.model.entity.Allocation;
import com.hemovia.api.model.entity.AllocationItem;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.BloodRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestCancellationStore {
    Optional<BloodRequest> lockRequest(UUID requestId);
    List<Allocation> findAllocations(UUID requestId);
    List<AllocationItem> findItems(UUID requestId);
    List<BloodBag> lockBags(List<UUID> ids);
    boolean hasInTransitTransport(UUID requestId);
    boolean hasOtherActiveReservations(List<UUID> ids, UUID requestId);
}
