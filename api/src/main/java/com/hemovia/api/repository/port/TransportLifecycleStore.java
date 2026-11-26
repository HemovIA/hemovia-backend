package com.hemovia.api.repository.port;

import com.hemovia.api.model.entity.Allocation;
import com.hemovia.api.model.entity.AllocationItem;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.entity.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportLifecycleStore {
    Optional<UUID> allocationRequestId(UUID id);
    Optional<UUID> transportRequestId(UUID id);
    Optional<BloodRequest> lockRequest(UUID id);
    Optional<Allocation> allocation(UUID id);
    Optional<Transport> transport(UUID id);
    boolean lockActiveCenter(UUID id);
    Optional<Vehicle> lockVehicle(UUID id);
    List<BloodBag> lockBags(List<UUID> ids);
    List<Allocation> allocations(UUID requestId);
    List<AllocationItem> items(UUID requestId);
    boolean otherReservations(List<UUID> ids, UUID requestId);
    boolean allocationHasTransport(UUID allocationId);
    boolean vehicleInTransit(UUID vehicleId);
    Transport save(Transport transport);
}
