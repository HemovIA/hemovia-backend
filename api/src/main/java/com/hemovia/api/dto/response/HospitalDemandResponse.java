package com.hemovia.api.dto.response;

import java.util.UUID;

public record HospitalDemandResponse(UUID hospitalId, String hospitalName, DemandMetricsResponse metrics) { }
