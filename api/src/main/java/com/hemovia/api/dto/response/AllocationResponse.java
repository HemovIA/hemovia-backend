package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.RequestStatus;
import java.util.List;
import java.util.UUID;

public record AllocationResponse(UUID id, UUID requestId, RequestStatus status, List<UUID> bloodBagIds) { }
