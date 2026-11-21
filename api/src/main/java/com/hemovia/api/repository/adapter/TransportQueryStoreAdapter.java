package com.hemovia.api.repository.adapter;
import com.hemovia.api.dto.internal.TransportQuery;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.repository.jpa.TransportRepository;
import com.hemovia.api.repository.port.TransportQueryStore;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;

@Repository
public class TransportQueryStoreAdapter implements TransportQueryStore {
    private final TransportRepository repository;
    public TransportQueryStoreAdapter(TransportRepository repository) { this.repository=repository; }
    public Page<Transport> find(TransportQuery filter, AuthenticatedUser user, int page, int size) {
        Specification<Transport> spec = (root, query, cb) -> {
            var clauses = new ArrayList<Predicate>();
            if (filter.id()!=null) clauses.add(cb.equal(root.get("id"), filter.id()));
            if (filter.status()!=null) clauses.add(cb.equal(root.get("status"), filter.status()));
            if (filter.requestId()!=null) clauses.add(cb.equal(root.get("allocation").get("request").get("id"), filter.requestId()));
            if (filter.vehicleId()!=null) clauses.add(cb.equal(root.get("vehicle").get("id"), filter.vehicleId()));
            switch(user.role()) {
                case ADMIN -> { }
                case HOSPITAL -> clauses.add(user.hospitalId()==null ? cb.disjunction() : cb.equal(root.get("allocation").get("request").get("hospital").get("id"), user.hospitalId()));
                case HEMOCENTRO -> clauses.add(user.bloodCenterId()==null ? cb.disjunction() : cb.equal(root.get("allocation").get("bloodCenter").get("id"), user.bloodCenterId()));
                case TRANSPORTADOR -> clauses.add(user.vehicleId()==null ? cb.disjunction() : cb.equal(root.get("vehicle").get("id"), user.vehicleId()));
            }
            return cb.and(clauses.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, PageRequest.of(page,size,Sort.by(Sort.Order.desc("startedAt"),Sort.Order.asc("id"))));
    }
}
