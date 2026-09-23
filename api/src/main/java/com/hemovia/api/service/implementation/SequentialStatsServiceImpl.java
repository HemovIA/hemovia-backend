package com.hemovia.api.service.implementation;

import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;
import com.hemovia.api.service.SequentialStatsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SequentialStatsServiceImpl implements SequentialStatsService {

    @Override
    public StatisticsResult calculateSequential(List<SyntheticRequest> requests) {
        validateRequests(requests);

        long sum = 0;
        long sumOfSquares = 0;

        for (SyntheticRequest request : requests) {
            int quantity = request.quantity();
            sum += quantity;
            sumOfSquares += (long) quantity * quantity;
        }

        return buildResult(sum, sumOfSquares, requests.size());
    }

    static void validateRequests(List<SyntheticRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Synthetic requests must not be null or empty.");
        }
    }

    static StatisticsResult buildResult(long sum, long sumOfSquares, long totalRecords) {
        double media = sum / (double) totalRecords;
        double variancia = (sumOfSquares / (double) totalRecords) - (media * media);
        return new StatisticsResult(media, Math.sqrt(Math.max(variancia, 0.0)), totalRecords);
    }
}
