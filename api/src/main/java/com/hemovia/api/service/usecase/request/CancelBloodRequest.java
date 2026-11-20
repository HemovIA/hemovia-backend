package com.hemovia.api.service.usecase.request;

import com.hemovia.api.dto.response.BloodRequestResponse;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.model.enums.AllocationStatus;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.ReservationStatus;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.model.exception.InvalidStateTransitionException;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.port.RequestCancellationStore;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CancelBloodRequest {
    private final RequestCancellationStore store;
    private final CurrentUserProvider currentUser;
    private final ClockProvider clock;

    public CancelBloodRequest(RequestCancellationStore store, CurrentUserProvider currentUser, ClockProvider clock) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BloodRequestResponse execute(UUID requestId) {
        var user = currentUser.current();
        if (!user.worksAtHospital(user.hospitalId())) {
            throw new InvalidDataException("Usuário sem vínculo com hospital.");
        }
        var request = store.lockRequest(requestId)
                .filter(r -> user.worksAtHospital(r.getHospital().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Requisição", requestId));
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.ALLOCATED) {
            throw new InvalidStateTransitionException("A requisição não pode ser cancelada neste estado.");
        }
        cancel(request, clock.now());
        return new BloodRequestResponse(request.getId(), request.getStatus());
    }
    private void cancel(BloodRequest request, Instant releasedAt) {
        var requestAllocations = store.findAllocations(request.getId());
        var requestItems = store.findItems(request.getId());
        if (store.hasInTransitTransport(request.getId())
                || requestAllocations.stream().anyMatch(a -> a.getStatus() == AllocationStatus.IN_TRANSIT
                    || a.getStatus() == AllocationStatus.DELIVERED)
                || requestItems.stream().anyMatch(i -> i.getStatus() == ReservationStatus.IN_TRANSIT
                    || i.getStatus() == ReservationStatus.DELIVERED)) {
            throw invalidState();
        }
        var activeAllocations = requestAllocations.stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED).toList();
        var reservedItems = requestItems.stream()
                .filter(i -> i.getStatus() == ReservationStatus.RESERVED).toList();
        if ((request.getStatus() == RequestStatus.PENDING && (!activeAllocations.isEmpty() || !reservedItems.isEmpty()))
                || (request.getStatus() == RequestStatus.ALLOCATED && (activeAllocations.isEmpty() || reservedItems.isEmpty()))
                || reservedItems.stream().anyMatch(i -> i.getAllocation().getStatus() != AllocationStatus.ALLOCATED)) {
            throw invalidState();
        }

        // Read only identifiers until the bag locks are acquired.
        var ids = reservedItems.stream().map(i -> i.getBloodBag().getId()).distinct().sorted().toList();
        var locked = store.lockBags(ids);
        if (locked.size() != ids.size() || reservedItems.size() != ids.size()
                || locked.stream().anyMatch(b -> b.getStatus() != BloodBagStatus.RESERVED)
                || store.hasOtherActiveReservations(ids, request.getId())) {
            throw invalidState();
        }

        for (var item : reservedItems) {
            item.setStatus(ReservationStatus.RELEASED);
            item.setReleasedAt(releasedAt);
        }
        locked.forEach(b -> b.setStatus(BloodBagStatus.AVAILABLE));
        activeAllocations.forEach(a -> a.setStatus(AllocationStatus.CANCELLED));
        request.setStatus(RequestStatus.CANCELLED);
    }

    private InvalidStateTransitionException invalidState() {
        return new InvalidStateTransitionException("A requisição ou suas reservas não podem ser canceladas neste estado.");
    }
}
