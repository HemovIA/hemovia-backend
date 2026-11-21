package com.hemovia.api.repository.adapter;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.Hospital;
import com.hemovia.api.repository.jpa.HospitalRepository;
import com.hemovia.api.repository.port.HospitalQueryStore;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
@Repository
public class HospitalQueryStoreAdapter implements HospitalQueryStore {
    private final HospitalRepository repository;
    public HospitalQueryStoreAdapter(HospitalRepository repository) { this.repository=repository; }
    public Page<Hospital> find(UUID id, String name, String city, AuthenticatedUser user, int page, int size) {
        Specification<Hospital> spec=(root, query, cb) -> {
            var clauses=new ArrayList<Predicate>();
            if(id!=null) clauses.add(cb.equal(root.get("id"),id));
            if(name!=null) clauses.add(cb.like(cb.lower(root.get("name")), pattern(name), '\\'));
            if(city!=null) clauses.add(cb.like(cb.lower(root.get("city")), pattern(city), '\\'));
            if(user.role()==com.hemovia.api.model.enums.Role.HOSPITAL)
                clauses.add(user.hospitalId()==null ? cb.disjunction() : cb.equal(root.get("id"),user.hospitalId()));
            else if(user.role()==com.hemovia.api.model.enums.Role.TRANSPORTADOR) clauses.add(cb.disjunction());
            return cb.and(clauses.toArray(Predicate[]::new));
        };
        return repository.findAll(spec,PageRequest.of(page,size,Sort.by(Sort.Order.asc("name"),Sort.Order.asc("id"))));
    }
    private static String pattern(String input) {
        return "%"+input.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")+"%";
    }
}
