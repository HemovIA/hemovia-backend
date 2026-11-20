package com.hemovia.api.dto.response;

import java.time.Instant;
import java.util.List;

public record DemandSummaryResponse(Instant from, Instant to, DemandMetricsResponse overall,
                                    List<HospitalDemandResponse> byHospital) {
    public DemandSummaryResponse {
        byHospital = List.copyOf(byHospital);
    }
}
