package com.hemovia.api.service.implementation;

import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;
import com.hemovia.api.service.ParallelStatsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ParallelStatsServiceImpl implements ParallelStatsService {

    @Override
    public StatisticsResult calculateParallel(List<SyntheticRequest> requests, int threadCount) {
        SequentialStatsServiceImpl.validateRequests(requests);
        validateThreadCount(threadCount);

        int workerCount = Math.min(threadCount, requests.size());
        int chunkSize = (requests.size() + workerCount - 1) / workerCount;
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);

        try {
            List<Future<PartialStatisticsResult>> futures = new ArrayList<>();
            for (int start = 0; start < requests.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, requests.size());
                List<SyntheticRequest> slice = requests.subList(start, end);
                futures.add(executorService.submit(() -> calculatePartial(slice)));
            }

            long totalSum = 0;
            long totalSumOfSquares = 0;
            long totalRecords = 0;

            for (Future<PartialStatisticsResult> future : futures) {
                PartialStatisticsResult partialStatisticsResult = getPartialResult(future);
                totalSum += partialStatisticsResult.soma();
                totalSumOfSquares += partialStatisticsResult.somaQuadrados();
                totalRecords += partialStatisticsResult.totalRegistros();
            }

            return SequentialStatsServiceImpl.buildResult(totalSum, totalSumOfSquares, totalRecords);
        } finally {
            executorService.shutdown();
        }
    }

    private PartialStatisticsResult calculatePartial(List<SyntheticRequest> slice) {
        long localSum = 0;
        long localSumOfSquares = 0;

        for (SyntheticRequest request : slice) {
            int quantity = request.quantity();
            localSum += quantity;
            localSumOfSquares += (long) quantity * quantity;
        }

        return new PartialStatisticsResult(localSum, localSumOfSquares, slice.size());
    }

    private PartialStatisticsResult getPartialResult(Future<PartialStatisticsResult> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Parallel benchmark execution was interrupted.", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Parallel benchmark execution failed.", exception.getCause());
        }
    }

    private void validateThreadCount(int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be greater than zero.");
        }
    }

    private record PartialStatisticsResult(long soma, long somaQuadrados, long totalRegistros) {
    }
}
