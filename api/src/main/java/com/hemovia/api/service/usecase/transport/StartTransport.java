package com.hemovia.api.service.usecase.transport;

import com.hemovia.api.dto.request.StartTransportRequest;
import com.hemovia.api.dto.response.TransportResponse;
import com.hemovia.api.model.algorithm.graph.DeliveryWindow;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.enums.AllocationStatus;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.ReservationStatus;
import com.hemovia.api.model.enums.TransportStatus;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.model.exception.VehicleUnavailableException;
import com.hemovia.api.repository.port.TransportLifecycleStore;
import com.hemovia.api.service.config.RoutingProperties;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartTransport {
    private final TransportLifecycleStore store;
    private final CurrentUserProvider currentUser;
    private final ClockProvider clock;
    private final RoutingProperties routing;

    public StartTransport(TransportLifecycleStore store, CurrentUserProvider currentUser, ClockProvider clock, RoutingProperties routing) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
        this.routing = routing;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransportResponse execute(StartTransportRequest input) {
        var requestId = store.allocationRequestId(input.allocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Alocação", input.allocationId()));
        var request = store.lockRequest(requestId).orElseThrow(() -> new ResourceNotFoundException("Alocação", input.allocationId()));
        var allocation = store.allocation(input.allocationId())
                .filter(a -> currentUser.current().worksAtBloodCenter(a.getBloodCenter().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Alocação", input.allocationId()));
        if (request.getStatus() != RequestStatus.ALLOCATED || allocation.getStatus() != AllocationStatus.ALLOCATED
                || store.allocationHasTransport(allocation.getId())) {
            throw TransportLifecycle.invalidState();
        }
        TransportLifecycle.validateRoute(allocation, request);
        if (!store.lockActiveCenter(allocation.getBloodCenter().getId())) {
            throw new ResourceNotFoundException("Hemocentro", allocation.getBloodCenter().getId());
        }
        var vehicle = store.lockVehicle(input.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", input.vehicleId()));
        var bags = TransportLifecycle.validateBatch(store, request, allocation, ReservationStatus.RESERVED, BloodBagStatus.RESERVED);
        var now = clock.now();
        DeliveryWindow.validate(request.getComponent(), request.getDeliveryDeadline(), now,
                allocation.getRoute().getEstimatedDurationSeconds(), routing.getPlateletsMaxSeconds());
        TransportLifecycle.validateDeparture(bags, request, now);
        if (vehicle.getCapacity() < bags.size()) {
            throw TransportLifecycle.invalidState();
        }
        if (store.vehicleInTransit(vehicle.getId())) {
            throw new VehicleUnavailableException();
        }
        var transport = new Transport();
        transport.setAllocation(allocation);
        transport.setRoute(allocation.getRoute());
        transport.setVehicle(vehicle);
        transport.setStatus(TransportStatus.IN_TRANSIT);
        transport.setStartedAt(now);
        request.setStatus(RequestStatus.IN_TRANSIT);
        allocation.setStatus(AllocationStatus.IN_TRANSIT);
        TransportLifecycle.activeItems(store, requestId).forEach(i -> i.setStatus(ReservationStatus.IN_TRANSIT));
        bags.forEach(b -> b.setStatus(BloodBagStatus.IN_TRANSIT));
        return TransportLifecycle.response(store.save(transport));
    }
}
