package com.hemovia.api.service.usecase.transport;
import com.hemovia.api.dto.internal.TransportQuery;
import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.*;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.port.TransportQueryStore;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import com.hemovia.api.service.usecase.request.QueryBloodRequests;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
public class QueryTransports {
    private final TransportQueryStore store;
    private final CurrentUserProvider currentUser;
    public QueryTransports(TransportQueryStore store, CurrentUserProvider currentUser) { this.store=store; this.currentUser=currentUser; }
    @Transactional(readOnly=true)
    public PageResponse<TransportSummaryResponse> list(TransportQuery filter, QueryPage page) {
        var found=store.find(filter,currentUser.current(),page.page(),page.size());
        return PageResponse.of(found.getContent().stream().map(QueryTransports::summary).toList(),page.page(),page.size(),found.getTotalElements());
    }
    @Transactional(readOnly=true)
    public TransportDetailsResponse detail(UUID id) {
        var found=store.find(new TransportQuery(id,null,null,null),currentUser.current(),0,1);
        var t=found.getContent().stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Transporte",id));
        return new TransportDetailsResponse(summary(t), QueryBloodRequests.route(t.getRoute()), t.getAllocation().getItems().stream().map(i -> i.getBloodBag().getId()).toList());
    }
    private static TransportSummaryResponse summary(Transport t) {
        var a=t.getAllocation();
        return new TransportSummaryResponse(t.getId(),a.getId(),a.getRequest().getId(),t.getRoute().getId(),t.getVehicle().getId(),t.getStatus(),t.getStartedAt(),t.getCompletedAt(),a.getRequest().getHospital().getId(),a.getBloodCenter().getId());
    }
}
