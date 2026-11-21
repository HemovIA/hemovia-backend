package com.hemovia.api.service.usecase.hospital;
import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.HospitalResponse;
import com.hemovia.api.dto.response.PageResponse;
import com.hemovia.api.model.entity.Hospital;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.port.HospitalQueryStore;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
public class QueryHospitals {
    private final HospitalQueryStore store;
    private final CurrentUserProvider currentUser;
    public QueryHospitals(HospitalQueryStore store, CurrentUserProvider currentUser) { this.store=store; this.currentUser=currentUser; }
    @Transactional(readOnly=true)
    public PageResponse<HospitalResponse> list(String name, String city, QueryPage page) {
        var user=currentUser.current();
        if(user.role()==com.hemovia.api.model.enums.Role.TRANSPORTADOR) throw new AccessDeniedException("Acesso negado");
        var found=store.find(null,name,city,user,page.page(),page.size());
        return PageResponse.of(found.getContent().stream().map(QueryHospitals::response).toList(),page.page(),page.size(),found.getTotalElements());
    }
    @Transactional(readOnly=true)
    public HospitalResponse detail(UUID id) {
        var user=currentUser.current();
        if(user.role()==com.hemovia.api.model.enums.Role.TRANSPORTADOR) throw new AccessDeniedException("Acesso negado");
        var found=store.find(id,null,null,user,0,1);
        return found.getContent().stream().map(QueryHospitals::response).findFirst().orElseThrow(() -> new ResourceNotFoundException("Hospital",id));
    }
    private static HospitalResponse response(Hospital h) { return new HospitalResponse(h.getId(),h.getName(),h.getCity(),h.getLatitude(),h.getLongitude(),h.getPriority()); }
}
