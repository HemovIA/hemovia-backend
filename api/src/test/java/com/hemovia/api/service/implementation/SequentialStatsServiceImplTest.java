package com.hemovia.api.service.implementation;

import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SequentialStatsServiceImplTest {

    private final SequentialStatsServiceImpl service = new SequentialStatsServiceImpl();

    @Test
    void shouldCalculatePopulationStatisticsSequentially() {
        List<SyntheticRequest> requests = List.of(
                new SyntheticRequest("HOSP-1", 10),
                new SyntheticRequest("HOSP-2", 20),
                new SyntheticRequest("HOSP-3", 30),
                new SyntheticRequest("HOSP-4", 40)
        );

        StatisticsResult result = service.calculateSequential(requests);

        assertThat(result.totalRegistros()).isEqualTo(4);
        assertThat(result.media()).isEqualTo(25.0);
        assertThat(result.desvioPadrao()).isCloseTo(11.180339887498949, within(1.0E-12));
    }
}
