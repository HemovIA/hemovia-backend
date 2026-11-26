package com.hemovia.api.service.usecase.request;

import com.hemovia.api.dto.internal.RequestQuery;
import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.*;
import com.hemovia.api.model.entity.*;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.jpa.AllocationRepository;
import com.hemovia.api.repository.jpa.TransportRepository;
import com.hemovia.api.repository.port.BloodRequestQueryStore;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class QueryBloodRequests {
    private final BloodRequestQueryStore store;
    private final AllocationRepository allocations;
    private final TransportRepository transports;
    private final CurrentUserProvider currentUser;
    public QueryBloodRequests(BloodRequestQueryStore store, AllocationRepository allocations, TransportRepository transports, CurrentUserProvider currentUser) {
        this.store = store; this.allocations = allocations; this.transports = transports; this.currentUser = currentUser;
    }
    @Transactional(readOnly = true)
    public PageResponse<BloodRequestSummaryResponse> list(RequestQuery filter, QueryPage page) {
        var found = store.find(filter, currentUser.current(), page.page(), page.size());
        return PageResponse.of(found.getContent().stream().map(QueryBloodRequests::summary).toList(), page.page(), page.size(), found.getTotalElements());
    }
    @Transactional(readOnly = true)
    public BloodRequestDetailsResponse detail(UUID id) {
        var found = store.find(new RequestQuery(id, null, null, null, null, null, null), currentUser.current(), 0, 1);
        var request = found.getContent().stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Requisição", id));
        var details = allocations.findByRequestId(id).stream().map(a -> new BloodRequestDetailsResponse.AllocationDetails(
                a.getId(), a.getBloodCenter().getId(), a.getStatus(), a.getCreatedAt(), a.getUpdatedAt(),
                a.getItems().stream().map(i -> new BloodRequestDetailsResponse.BagDetails(i.getBloodBag().getId(), i.getStatus())).toList(),
                route(a.getRoute()), transports.findByAllocationId(a.getId()).map(Transport::getId).orElse(null))).toList();
        return new BloodRequestDetailsResponse(summary(request), details);
    }
    public static BloodRequestSummaryResponse summary(BloodRequest r) {
        return new BloodRequestSummaryResponse(r.getId(), r.getHospital().getId(), r.getHospital().getName(), r.getBloodType(),
                r.getRhFactor(), r.getComponent(), r.getQuantity(), r.getPriority(), r.getDeliveryDeadline(), r.getStatus(), r.getCreatedAt(), r.getUpdatedAt());
    }
    public static BloodRequestDetailsResponse.StoredRoute route(DeliveryRoute r) {
        if (r == null) return null;
        return new BloodRequestDetailsResponse.StoredRoute(r.getId(), r.getDistanceKm(), r.getEstimatedDurationSeconds(),
                List.copyOf(r.getNodeIds()), r.getWaypoints().stream().map(w -> List.of(w.getLongitude(), w.getLatitude())).toList());
    }
}
