package com.hemovia.api.repository.port;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.Hospital;
import org.springframework.data.domain.Page;
import java.util.UUID;
public interface HospitalQueryStore {
    Page<Hospital> find(UUID id, String name, String city, AuthenticatedUser user, int page, int size);
}
