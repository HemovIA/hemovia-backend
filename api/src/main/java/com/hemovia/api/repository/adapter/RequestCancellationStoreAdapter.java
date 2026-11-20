package com.hemovia.api.repository.adapter;

import com.hemovia.api.model.entity.Allocation;
import com.hemovia.api.model.entity.AllocationItem;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.repository.jpa.AllocationItemRepository;
import com.hemovia.api.repository.jpa.AllocationRepository;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import com.hemovia.api.repository.jpa.BloodRequestRepository;
import com.hemovia.api.repository.jpa.TransportRepository;
import com.hemovia.api.repository.port.RequestCancellationStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RequestCancellationStoreAdapter implements RequestCancellationStore {
    private final BloodRequestRepository requests;
    private final AllocationRepository allocations;
    private final AllocationItemRepository items;
    private final BloodBagRepository bags;
    private final TransportRepository transports;

    public RequestCancellationStoreAdapter(BloodRequestRepository requests, AllocationRepository allocations,
            AllocationItemRepository items, BloodBagRepository bags, TransportRepository transports) {
        this.requests = requests;
        this.allocations = allocations;
        this.items = items;
        this.bags = bags;
        this.transports = transports;
    }

    @Override
    public Optional<BloodRequest> lockRequest(UUID requestId) {
        return requests.lockById(requestId);
    }

    @Override
    public List<Allocation> findAllocations(UUID requestId) {
        return allocations.findByRequestId(requestId);
    }

    @Override
    public List<AllocationItem> findItems(UUID requestId) {
        return items.findByRequestId(requestId);
    }

    @Override
    public List<BloodBag> lockBags(List<UUID> ids) {
        return ids.isEmpty() ? List.of() : bags.lockByIds(ids);
    }

    @Override
    public boolean hasInTransitTransport(UUID requestId) {
        return transports.countInTransitByRequestId(requestId) > 0;
    }

    @Override
    public boolean hasOtherActiveReservations(List<UUID> ids, UUID requestId) {
        return !ids.isEmpty() && items.countOtherActiveReservations(ids, requestId) > 0;
    }
}
