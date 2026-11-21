package com.hemovia.api.service.usecase.statistics;

import com.hemovia.api.dto.internal.DemandCount;
import com.hemovia.api.dto.response.DemandMetricsResponse;
import com.hemovia.api.dto.response.DemandSummaryResponse;
import com.hemovia.api.dto.response.HospitalDemandResponse;
import com.hemovia.api.model.algorithm.statistics.DescriptiveStatistics;
import com.hemovia.api.model.algorithm.statistics.Frequency;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.repository.port.StatisticsStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class CalculateDemandStatistics {
    private static final String WARNING = "Amostra insuficiente para dispersão (n < 2).";
    private final StatisticsStore store;

    public CalculateDemandStatistics(StatisticsStore store) {
        this.store = store;
    }

    @Transactional(readOnly = true)
    public DemandSummaryResponse execute(Instant from, Instant to) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new InvalidDataException("O início do período deve ser anterior ao fim.");
        }
        List<Frequency> overall = new ArrayList<>();
        Map<UUID, HospitalFrequencies> hospitals = new TreeMap<>();
        for (DemandCount row : store.demand(from, to)) {
            Frequency frequency = new Frequency(row.quantity(), row.count());
            overall.add(frequency);
            hospitals.computeIfAbsent(row.hospitalId(), id -> new HospitalFrequencies(row.hospitalName(), new ArrayList<>()))
                    .frequencies().add(frequency);
        }
        List<HospitalDemandResponse> byHospital = new ArrayList<>();
        hospitals.forEach((id, data) -> byHospital.add(new HospitalDemandResponse(id, data.name(), metrics(data.frequencies()))));
        return new DemandSummaryResponse(from, to, metrics(overall), byHospital);
    }

    private static DemandMetricsResponse metrics(List<Frequency> frequencies) {
        DescriptiveStatistics.Result result = DescriptiveStatistics.calculate(frequencies);
        return new DemandMetricsResponse(result.count(), result.total(), result.mean(), result.median(),
                result.populationVariance(), result.populationStandardDeviation(), result.sampleVariance(),
                result.sampleStandardDeviation(), result.insufficientSample(), result.insufficientSample() ? WARNING : null);
    }

    private record HospitalFrequencies(String name, List<Frequency> frequencies) { }
}
