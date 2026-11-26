package com.hemovia.api.repository.port;

import com.hemovia.api.dto.internal.RequestQuery;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.entity.BloodRequest;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface BloodRequestQueryStore {
    Page<BloodRequest> find(RequestQuery query, AuthenticatedUser user, int page, int size);
}
