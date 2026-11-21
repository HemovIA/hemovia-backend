package com.hemovia.api.repository.adapter;

import com.hemovia.api.dto.internal.RequestQuery;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.*;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.repository.jpa.BloodRequestRepository;
import com.hemovia.api.repository.port.BloodRequestQueryStore;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;

@Repository
public class BloodRequestQueryStoreAdapter implements BloodRequestQueryStore {
    private final BloodRequestRepository repository;
    public BloodRequestQueryStoreAdapter(BloodRequestRepository repository) { this.repository = repository; }
    public Page<BloodRequest> find(RequestQuery filter, AuthenticatedUser user, int page, int size) {
        Specification<BloodRequest> spec = (root, query, cb) -> {
            var clauses = new ArrayList<Predicate>();
            if (filter.id() != null) clauses.add(cb.equal(root.get("id"), filter.id()));
            if (filter.status() != null) clauses.add(cb.equal(root.get("status"), filter.status()));
            if (filter.hospitalId() != null) clauses.add(cb.equal(root.get("hospital").get("id"), filter.hospitalId()));
            if (filter.bloodType() != null) clauses.add(cb.equal(root.get("bloodType"), filter.bloodType()));
            if (filter.rhFactor() != null) clauses.add(cb.equal(root.get("rhFactor"), filter.rhFactor()));
            if (filter.component() != null) clauses.add(cb.equal(root.get("component"), filter.component()));
            if (filter.priority() != null) clauses.add(cb.equal(root.get("priority"), filter.priority()));
            switch (user.role()) {
                case ADMIN -> { }
                case HOSPITAL -> clauses.add(user.hospitalId() == null ? cb.disjunction() : cb.equal(root.get("hospital").get("id"), user.hospitalId()));
                case HEMOCENTRO -> {
                    if (user.bloodCenterId() == null) clauses.add(cb.disjunction());
                    else {
                        var sub = query.subquery(Integer.class);
                        var a = sub.from(Allocation.class);
                        sub.select(cb.literal(1)).where(cb.equal(a.get("request"), root), cb.equal(a.get("bloodCenter").get("id"), user.bloodCenterId()));
                        clauses.add(cb.or(cb.equal(root.get("status"), RequestStatus.PENDING), cb.exists(sub)));
                    }
                }
                case TRANSPORTADOR -> {
                    if (user.vehicleId() == null) clauses.add(cb.disjunction());
                    else {
                        var sub = query.subquery(Integer.class);
                        var t = sub.from(Transport.class);
                        sub.select(cb.literal(1)).where(cb.equal(t.get("allocation").get("request"), root), cb.equal(t.get("vehicle").get("id"), user.vehicleId()));
                        clauses.add(cb.exists(sub));
                    }
                }
            }
            return cb.and(clauses.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))));
    }
}
