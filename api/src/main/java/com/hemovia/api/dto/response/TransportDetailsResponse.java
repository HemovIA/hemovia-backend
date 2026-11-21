package com.hemovia.api.dto.response;
import java.util.List;
import java.util.UUID;
public record TransportDetailsResponse(TransportSummaryResponse transport, BloodRequestDetailsResponse.StoredRoute route, List<UUID> bloodBagIds) { }
