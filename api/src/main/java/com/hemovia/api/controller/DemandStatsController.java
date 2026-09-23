package com.hemovia.api.controller;

import com.hemovia.api.generator.SyntheticRequestGenerator;
import com.hemovia.api.model.DTOs.BenchmarkRequestDTO;
import com.hemovia.api.model.DTOs.BenchmarkResponseDTO;
import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;
import com.hemovia.api.service.ParallelStatsService;
import com.hemovia.api.service.SequentialStatsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/benchmark")
public class DemandStatsController {

    private static final List<Integer> DEFAULT_THREAD_COUNTS = List.of(2, 4, 8);
    private static final int DEFAULT_EXECUTION_COUNT = 5;
    private static final double RESULT_TOLERANCE = 1.0E-9;

    private final SyntheticRequestGenerator syntheticRequestGenerator;
    private final SequentialStatsService sequentialStatsService;
    private final ParallelStatsService parallelStatsService;

    public DemandStatsController(SyntheticRequestGenerator syntheticRequestGenerator,
                                 SequentialStatsService sequentialStatsService,
                                 ParallelStatsService parallelStatsService) {
        this.syntheticRequestGenerator = syntheticRequestGenerator;
        this.sequentialStatsService = sequentialStatsService;
        this.parallelStatsService = parallelStatsService;
    }

    @PostMapping("/demanda-stats")
    public ResponseEntity<BenchmarkResponseDTO> calcular(@Valid @RequestBody BenchmarkRequestDTO benchmarkRequestDTO) {
        List<SyntheticRequest> dados = syntheticRequestGenerator.generate(benchmarkRequestDTO.getQuantidadeRegistros());
        int executionCount = resolveExecutionCount(benchmarkRequestDTO.getQuantidadeExecucoes());
        List<Integer> threadCounts = resolveThreadCounts(benchmarkRequestDTO.getQuantidadesThreads());

        BenchmarkExecution sequentialExecution =
                benchmark(() -> sequentialStatsService.calculateSequential(dados), executionCount);

        List<BenchmarkResponseDTO.ParallelBenchmarkDTO> parallelBenchmarks = new ArrayList<>();
        for (Integer threadCount : threadCounts) {
            BenchmarkExecution parallelExecution =
                    benchmark(() -> parallelStatsService.calculateParallel(dados, threadCount), executionCount);
            boolean equivalentToSequential = areEquivalent(sequentialExecution.result(), parallelExecution.result());

            if (!equivalentToSequential) {
                throw new IllegalStateException(
                        "Parallel benchmark diverged from sequential result for " + threadCount + " threads."
                );
            }

            parallelBenchmarks.add(new BenchmarkResponseDTO.ParallelBenchmarkDTO(
                    threadCount,
                    toResultDTO(parallelExecution.result()),
                    toTimingDTO(parallelExecution),
                    true
            ));
        }

        BenchmarkResponseDTO response = new BenchmarkResponseDTO();
        response.setQuantidadeRegistros(dados.size());
        response.setQuantidadeExecucoesMedidas(executionCount - 1);
        response.setResultadoSequencial(toResultDTO(sequentialExecution.result()));
        response.setTempoSequencial(toTimingDTO(sequentialExecution));
        response.setExecucoesParalelas(parallelBenchmarks);

        return ResponseEntity.ok(response);
    }

    private BenchmarkExecution benchmark(Supplier<StatisticsResult> supplier, int executionCount) {
        StatisticsResult warmUpResult = supplier.get();
        List<Long> measuredRunTimes = new ArrayList<>();
        StatisticsResult lastMeasuredResult = warmUpResult;

        for (int run = 1; run < executionCount; run++) {
            long start = System.nanoTime();
            lastMeasuredResult = supplier.get();
            measuredRunTimes.add(System.nanoTime() - start);
        }

        return new BenchmarkExecution(lastMeasuredResult, measuredRunTimes);
    }

    private BenchmarkResponseDTO.StatisticsResultDTO toResultDTO(StatisticsResult statisticsResult) {
        return new BenchmarkResponseDTO.StatisticsResultDTO(
                statisticsResult.media(),
                statisticsResult.desvioPadrao(),
                statisticsResult.totalRegistros()
        );
    }

    private BenchmarkResponseDTO.BenchmarkTimingDTO toTimingDTO(BenchmarkExecution benchmarkExecution) {
        long averageTime = Math.round(
                benchmarkExecution.measuredRunTimes().stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0)
        );

        return new BenchmarkResponseDTO.BenchmarkTimingDTO(
                List.copyOf(benchmarkExecution.measuredRunTimes()),
                averageTime
        );
    }

    private List<Integer> resolveThreadCounts(List<Integer> requestedThreadCounts) {
        if (requestedThreadCounts == null || requestedThreadCounts.isEmpty()) {
            return DEFAULT_THREAD_COUNTS;
        }

        return new ArrayList<>(new LinkedHashSet<>(requestedThreadCounts));
    }

    private int resolveExecutionCount(Integer requestedExecutionCount) {
        return requestedExecutionCount == null ? DEFAULT_EXECUTION_COUNT : requestedExecutionCount;
    }

    private boolean areEquivalent(StatisticsResult sequentialResult, StatisticsResult parallelResult) {
        return sequentialResult.totalRegistros() == parallelResult.totalRegistros()
                && Math.abs(sequentialResult.media() - parallelResult.media()) <= RESULT_TOLERANCE
                && Math.abs(sequentialResult.desvioPadrao() - parallelResult.desvioPadrao()) <= RESULT_TOLERANCE;
    }

    private record BenchmarkExecution(StatisticsResult result, List<Long> measuredRunTimes) {
    }
}
