package com.hemovia.api.repository.adapter;

import com.hemovia.api.model.entity.Allocation;
import com.hemovia.api.model.entity.AllocationItem;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.entity.Vehicle;
import com.hemovia.api.model.enums.TransportStatus;
import com.hemovia.api.repository.jpa.AllocationItemRepository;
import com.hemovia.api.repository.jpa.AllocationRepository;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import com.hemovia.api.repository.jpa.BloodCenterRepository;
import com.hemovia.api.repository.jpa.BloodRequestRepository;
import com.hemovia.api.repository.jpa.TransportRepository;
import com.hemovia.api.repository.jpa.VehicleRepository;
import com.hemovia.api.repository.port.TransportLifecycleStore;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransportLifecycleStoreAdapter implements TransportLifecycleStore {
    private final AllocationRepository allocations;
    private final AllocationItemRepository items;
    private final BloodBagRepository bags;
    private final BloodCenterRepository centers;
    private final BloodRequestRepository requests;
    private final TransportRepository transports;
    private final VehicleRepository vehicles;

    public TransportLifecycleStoreAdapter(AllocationRepository allocations, AllocationItemRepository items,
            BloodBagRepository bags, BloodCenterRepository centers, BloodRequestRepository requests,
            TransportRepository transports, VehicleRepository vehicles) {
        this.allocations = allocations;
        this.items = items;
        this.bags = bags;
        this.centers = centers;
        this.requests = requests;
        this.transports = transports;
        this.vehicles = vehicles;
    }

    @Override
    public Optional<UUID> allocationRequestId(UUID id) {
        return allocations.findRequestId(id);
    }

    @Override
    public Optional<UUID> transportRequestId(UUID id) {
        return transports.findRequestId(id);
    }

    @Override
    public Optional<BloodRequest> lockRequest(UUID id) {
        return requests.lockById(id);
    }

    @Override
    public Optional<Allocation> allocation(UUID id) {
        return allocations.findById(id);
    }

    @Override
    public Optional<Transport> transport(UUID id) {
        return transports.findById(id);
    }

    @Override
    public boolean lockActiveCenter(UUID id) {
        return centers.findActiveForUse(id).isPresent();
    }

    @Override
    public Optional<Vehicle> lockVehicle(UUID id) {
        return vehicles.lockById(id);
    }

    @Override
    public List<BloodBag> lockBags(List<UUID> ids) {
        return ids.isEmpty() ? List.of() : bags.lockByIds(ids);
    }

    @Override
    public List<Allocation> allocations(UUID requestId) {
        return allocations.findByRequestId(requestId);
    }

    @Override
    public List<AllocationItem> items(UUID requestId) {
        return items.findByRequestId(requestId);
    }

    @Override
    public boolean otherReservations(List<UUID> ids, UUID requestId) {
        return !ids.isEmpty() && items.countOtherActiveReservations(ids, requestId) > 0;
    }

    @Override
    public boolean allocationHasTransport(UUID allocationId) {
        return transports.existsByAllocationId(allocationId);
    }

    @Override
    public boolean vehicleInTransit(UUID vehicleId) {
        return transports.existsByVehicleIdAndStatus(vehicleId, TransportStatus.IN_TRANSIT);
    }

    @Override
    public Transport save(Transport transport) {
        return transports.save(transport);
    }

}
