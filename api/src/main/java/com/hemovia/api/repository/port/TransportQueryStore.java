package com.hemovia.api.repository.port;
import com.hemovia.api.dto.internal.TransportQuery;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.Transport;
import org.springframework.data.domain.Page;
public interface TransportQueryStore {
    Page<Transport> find(TransportQuery filter, AuthenticatedUser user, int page, int size);
}
