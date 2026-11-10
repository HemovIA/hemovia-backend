package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.RequestAllocationStore;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.model.Allocation;
import com.hemovia.api.model.AllocationItem;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.BloodRequest;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.repository.AllocationRepository;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.repository.BloodCenterRepository;
import com.hemovia.api.repository.BloodRequestRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RequestAllocationStoreAdapter implements RequestAllocationStore {
    private final BloodRequestRepository requests;
    private final BloodBagRepository bags;
    private final BloodCenterRepository centers;
    private final AllocationRepository allocations;

    public RequestAllocationStoreAdapter(BloodRequestRepository requests, BloodBagRepository bags,
                                         BloodCenterRepository centers, AllocationRepository allocations) {
        this.requests = requests;
        this.bags = bags;
        this.centers = centers;
        this.allocations = allocations;
    }

    @Override
    public Optional<BloodRequest> lockRequest(UUID requestId) {
        return requests.lockById(requestId);
    }

    @Override
    public List<BloodBag> lockEligibleBags(UUID centerId, BloodComponent component, LocalDate today) {
        centers.findActiveForUse(centerId).orElseThrow(() -> new ResourceNotFoundException("Hemocentro", centerId));
        var locked = bags.lockEligible(centerId, component, today);
        if (locked.isEmpty()) {
            return locked;
        }
        // Uma nova consulta após obter os bloqueios também enxerga reservas confirmadas durante a espera.
        var reserved = new HashSet<>(bags.findActivelyReservedIds(locked.stream().map(BloodBag::getId).toList()));
        return locked.stream()
                .filter(bag -> bag.getStatus() == BloodBagStatus.AVAILABLE && !reserved.contains(bag.getId()))
                .toList();
    }

    @Override
    public Allocation reserve(BloodRequest request, UUID centerId, List<BloodBag> selected) {
        var allocation = new Allocation();
        allocation.setRequest(request);
        allocation.setBloodCenter(centers.getReferenceById(centerId));
        for (var bag : selected) {
            bag.setStatus(BloodBagStatus.RESERVED);
            var item = new AllocationItem();
            item.setBloodBag(bag);
            allocation.addItem(item);
        }
        request.setStatus(RequestStatus.ALLOCATED);
        return allocations.save(allocation);
    }
}
