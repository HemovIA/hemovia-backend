package com.hemovia.api.service.implementation;

import com.hemovia.api.generator.SyntheticRequestGenerator;
import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ParallelStatsServiceImplTest {

    private final SyntheticRequestGenerator syntheticRequestGenerator = new SyntheticRequestGenerator();
    private final SequentialStatsServiceImpl sequentialStatsService = new SequentialStatsServiceImpl();
    private final ParallelStatsServiceImpl parallelStatsService = new ParallelStatsServiceImpl();

    @Test
    void shouldMatchSequentialStatisticsAcrossThreadCounts() {
        List<SyntheticRequest> requests = syntheticRequestGenerator.generate(10_000);
        StatisticsResult sequentialResult = sequentialStatsService.calculateSequential(requests);

        for (int threadCount : List.of(2, 4, 8)) {
            StatisticsResult parallelResult = parallelStatsService.calculateParallel(requests, threadCount);

            assertThat(parallelResult.totalRegistros()).isEqualTo(sequentialResult.totalRegistros());
            assertThat(parallelResult.media()).isCloseTo(sequentialResult.media(), within(1.0E-12));
            assertThat(parallelResult.desvioPadrao())
                    .isCloseTo(sequentialResult.desvioPadrao(), within(1.0E-12));
        }
    }

    @Test
    void shouldRejectInvalidThreadCount() {
        assertThatThrownBy(() -> parallelStatsService.calculateParallel(List.of(new SyntheticRequest("HOSP-1", 5)), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Thread count");
    }
}
