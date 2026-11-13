package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.RequestStatus;
import java.util.UUID;

public record BloodRequestResponse(UUID id, RequestStatus status) { }
