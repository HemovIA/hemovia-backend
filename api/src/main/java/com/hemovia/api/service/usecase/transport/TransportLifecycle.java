package com.hemovia.api.service.usecase.transport;

import com.hemovia.api.dto.response.TransportResponse;
import com.hemovia.api.model.algorithm.compatibility.BloodCompatibility;
import com.hemovia.api.model.entity.Allocation;
import com.hemovia.api.model.entity.AllocationItem;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.enums.AllocationStatus;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.ReservationStatus;
import com.hemovia.api.model.enums.RhFactor;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.exception.InvalidStateTransitionException;
import com.hemovia.api.repository.port.TransportLifecycleStore;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

final class TransportLifecycle {
    private TransportLifecycle() { }

    static List<BloodBag> validateBatch(TransportLifecycleStore store, BloodRequest request, Allocation allocation,
                                        ReservationStatus itemStatus, BloodBagStatus bagStatus) {
        var active = store.allocations(request.getId()).stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED || a.getStatus() == AllocationStatus.IN_TRANSIT)
                .toList();
        var requestItems = store.items(request.getId());
        var items = requestItems.stream()
                .filter(i -> i.getAllocation().getId().equals(allocation.getId()))
                .toList();
        boolean otherActiveItems = requestItems.stream().anyMatch(i ->
                !i.getAllocation().getId().equals(allocation.getId())
                    && (i.getStatus() == ReservationStatus.RESERVED || i.getStatus() == ReservationStatus.IN_TRANSIT));
        if (active.size() != 1 || !active.get(0).getId().equals(allocation.getId())
                || otherActiveItems || request.getQuantity() == null || items.size() != request.getQuantity()
                || items.stream().anyMatch(i -> !i.getAllocation().getId().equals(allocation.getId())
                    || i.getStatus() != itemStatus || i.getReleasedAt() != null)) {
            throw invalidState();
        }
        var ids = items.stream().map(i -> i.getBloodBag().getId()).distinct().sorted().toList();
        if (ids.size() != items.size() || ids.isEmpty()) {
            throw invalidState();
        }
        var bags = store.lockBags(ids);
        UUID centerId = allocation.getBloodCenter().getId();
        if (bags.size() != ids.size() || store.otherReservations(ids, request.getId())
                || bags.stream().anyMatch(b -> b.getStatus() != bagStatus || b.getBloodCenter() == null
                    || !centerId.equals(b.getBloodCenter().getId()) || b.getComponent() != request.getComponent())) {
            throw invalidState();
        }
        return bags;
    }

    static void validateDeparture(List<BloodBag> bags, BloodRequest request, Instant departure) {
        if (bags.stream().anyMatch(b -> b.getExpirationDate() == null
                || !b.getExpirationDate().isAfter(departure.atZone(ZoneOffset.UTC).toLocalDate())
                || !BloodCompatibility.canTransfuse(b.getBloodTypeEnum(), b.getRhFactor() == RhFactor.POSITIVE,
                    request.getBloodType(), request.getRhFactor() == RhFactor.POSITIVE))) {
            throw invalidState();
        }
    }

    static List<AllocationItem> activeItems(TransportLifecycleStore store, UUID requestId) {
        return store.items(requestId).stream().filter(i -> i.getStatus() == ReservationStatus.RESERVED
                || i.getStatus() == ReservationStatus.IN_TRANSIT).toList();
    }

    static void validateRoute(Allocation allocation, BloodRequest request) {
        var route = allocation.getRoute();
        if (route == null || route.getOrigin() == null || route.getDestination() == null
                || !allocation.getBloodCenter().getId().equals(route.getOrigin().getId())
                || !request.getHospital().getId().equals(route.getDestination().getId())
                || route.getEstimatedDurationSeconds() == null || route.getEstimatedDurationSeconds() <= 0) {
            throw new InvalidRouteException("A rota da alocação é inválida.");
        }
    }

    static InvalidStateTransitionException invalidState() {
        return new InvalidStateTransitionException("O transporte ou suas reservas estão em estado inválido.");
    }

    static TransportResponse response(Transport t) {
        return new TransportResponse(t.getId(), t.getAllocation().getId(), t.getAllocation().getRequest().getId(),
                t.getRoute().getId(), t.getVehicle().getId(), t.getStatus(), t.getStartedAt(), t.getCompletedAt());
    }
}
