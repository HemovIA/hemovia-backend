package com.hemovia.api.service.usecase.transport;

import com.hemovia.api.dto.response.TransportResponse;
import com.hemovia.api.model.enums.AllocationStatus;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.ReservationStatus;
import com.hemovia.api.model.enums.TransportStatus;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.port.TransportLifecycleStore;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CompleteTransport {
    private final TransportLifecycleStore store;
    private final CurrentUserProvider currentUser;
    private final ClockProvider clock;

    public CompleteTransport(TransportLifecycleStore store, CurrentUserProvider currentUser, ClockProvider clock) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransportResponse execute(UUID id) {
        var requestId = store.transportRequestId(id).orElseThrow(() -> new ResourceNotFoundException("Transporte", id));
        var request = store.lockRequest(requestId).orElseThrow(() -> new ResourceNotFoundException("Transporte", id));
        if (!currentUser.current().worksAtHospital(request.getHospital().getId())) {
            throw new ResourceNotFoundException("Transporte", id);
        }
        var transport = store.transport(id).orElseThrow(() -> new ResourceNotFoundException("Transporte", id));
        var allocation = transport.getAllocation();
        if (transport.getStatus() != TransportStatus.IN_TRANSIT || request.getStatus() != RequestStatus.IN_TRANSIT
                || allocation.getStatus() != AllocationStatus.IN_TRANSIT || transport.getStartedAt() == null
                || transport.getCompletedAt() != null || allocation.getRoute() == null
                || !allocation.getRoute().getId().equals(transport.getRoute().getId())) {
            throw TransportLifecycle.invalidState();
        }
        TransportLifecycle.validateRoute(allocation, request);
        store.lockVehicle(transport.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", transport.getVehicle().getId()));
        var bags = TransportLifecycle.validateBatch(store, request, allocation, ReservationStatus.IN_TRANSIT, BloodBagStatus.IN_TRANSIT);
        var now = clock.now();
        if (now.isBefore(transport.getStartedAt())) {
            throw TransportLifecycle.invalidState();
        }
        transport.setStatus(TransportStatus.DELIVERED);
        transport.setCompletedAt(now);
        request.setStatus(RequestStatus.DELIVERED);
        allocation.setStatus(AllocationStatus.DELIVERED);
        TransportLifecycle.activeItems(store, requestId).forEach(i -> i.setStatus(ReservationStatus.DELIVERED));
        bags.forEach(b -> b.setStatus(BloodBagStatus.DELIVERED));
        return TransportLifecycle.response(transport);
    }
}
