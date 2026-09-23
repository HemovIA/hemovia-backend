package com.hemovia.api.model.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BenchmarkRequestDTO {

    @NotNull
    @Min(1)
    private Integer quantidadeRegistros;

    @Min(2)
    private Integer quantidadeExecucoes = 5;

    private List<@NotNull @Min(1) Integer> quantidadesThreads = List.of(2, 4, 8);

    public Integer getQuantidadeRegistros() {
        return quantidadeRegistros;
    }

    public void setQuantidadeRegistros(Integer quantidadeRegistros) {
        this.quantidadeRegistros = quantidadeRegistros;
    }

    public Integer getQuantidadeExecucoes() {
        return quantidadeExecucoes;
    }

    public void setQuantidadeExecucoes(Integer quantidadeExecucoes) {
        this.quantidadeExecucoes = quantidadeExecucoes;
    }

    public List<Integer> getQuantidadesThreads() {
        return quantidadesThreads;
    }

    public void setQuantidadesThreads(List<Integer> quantidadesThreads) {
        this.quantidadesThreads = quantidadesThreads;
    }
}
