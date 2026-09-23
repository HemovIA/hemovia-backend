package com.hemovia.api.controller;

import com.hemovia.api.generator.SyntheticRequestGenerator;
import com.hemovia.api.model.DTOs.BenchmarkRequestDTO;
import com.hemovia.api.model.DTOs.BenchmarkResponseDTO;
import com.hemovia.api.service.implementation.ParallelStatsServiceImpl;
import com.hemovia.api.service.implementation.SequentialStatsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemandStatsControllerTest {

    private final DemandStatsController controller = new DemandStatsController(
            new SyntheticRequestGenerator(),
            new SequentialStatsServiceImpl(),
            new ParallelStatsServiceImpl()
    );

    @Test
    void shouldReturnBenchmarkPayload() {
        BenchmarkRequestDTO request = new BenchmarkRequestDTO();
        request.setQuantidadeRegistros(1_000);
        request.setQuantidadeExecucoes(3);
        request.setQuantidadesThreads(List.of(2, 4));

        ResponseEntity<BenchmarkResponseDTO> response = controller.calcular(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantidadeRegistros()).isEqualTo(1_000);
        assertThat(response.getBody().getQuantidadeExecucoesMedidas()).isEqualTo(2);
        assertThat(response.getBody().getResultadoSequencial().getTotalRegistros()).isEqualTo(1_000);
        assertThat(response.getBody().getExecucoesParalelas()).hasSize(2);
        assertThat(response.getBody().getExecucoesParalelas().get(0).getQuantidadeThreads()).isEqualTo(2);
        assertThat(response.getBody().getExecucoesParalelas().get(1).getQuantidadeThreads()).isEqualTo(4);
        assertThat(response.getBody().getExecucoesParalelas()).allMatch(BenchmarkResponseDTO.ParallelBenchmarkDTO::isEquivalenteAoSequencial);
    }
}
