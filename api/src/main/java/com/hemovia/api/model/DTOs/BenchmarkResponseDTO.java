package com.hemovia.api.model.DTOs;

import java.util.List;

public class BenchmarkResponseDTO {

    private int quantidadeRegistros;
    private int quantidadeExecucoesMedidas;
    private StatisticsResultDTO resultadoSequencial;
    private BenchmarkTimingDTO tempoSequencial;
    private List<ParallelBenchmarkDTO> execucoesParalelas;

    public int getQuantidadeRegistros() {
        return quantidadeRegistros;
    }

    public void setQuantidadeRegistros(int quantidadeRegistros) {
        this.quantidadeRegistros = quantidadeRegistros;
    }

    public int getQuantidadeExecucoesMedidas() {
        return quantidadeExecucoesMedidas;
    }

    public void setQuantidadeExecucoesMedidas(int quantidadeExecucoesMedidas) {
        this.quantidadeExecucoesMedidas = quantidadeExecucoesMedidas;
    }

    public StatisticsResultDTO getResultadoSequencial() {
        return resultadoSequencial;
    }

    public void setResultadoSequencial(StatisticsResultDTO resultadoSequencial) {
        this.resultadoSequencial = resultadoSequencial;
    }

    public BenchmarkTimingDTO getTempoSequencial() {
        return tempoSequencial;
    }

    public void setTempoSequencial(BenchmarkTimingDTO tempoSequencial) {
        this.tempoSequencial = tempoSequencial;
    }

    public List<ParallelBenchmarkDTO> getExecucoesParalelas() {
        return execucoesParalelas;
    }

    public void setExecucoesParalelas(List<ParallelBenchmarkDTO> execucoesParalelas) {
        this.execucoesParalelas = execucoesParalelas;
    }

    public static class StatisticsResultDTO {
        private double media;
        private double desvioPadrao;
        private long totalRegistros;

        public StatisticsResultDTO() {
        }

        public StatisticsResultDTO(double media, double desvioPadrao, long totalRegistros) {
            this.media = media;
            this.desvioPadrao = desvioPadrao;
            this.totalRegistros = totalRegistros;
        }

        public double getMedia() {
            return media;
        }

        public void setMedia(double media) {
            this.media = media;
        }

        public double getDesvioPadrao() {
            return desvioPadrao;
        }

        public void setDesvioPadrao(double desvioPadrao) {
            this.desvioPadrao = desvioPadrao;
        }

        public long getTotalRegistros() {
            return totalRegistros;
        }

        public void setTotalRegistros(long totalRegistros) {
            this.totalRegistros = totalRegistros;
        }
    }

    public static class BenchmarkTimingDTO {
        private List<Long> execucoesNanos;
        private long mediaNanos;

        public BenchmarkTimingDTO() {
        }

        public BenchmarkTimingDTO(List<Long> execucoesNanos, long mediaNanos) {
            this.execucoesNanos = execucoesNanos;
            this.mediaNanos = mediaNanos;
        }

        public List<Long> getExecucoesNanos() {
            return execucoesNanos;
        }

        public void setExecucoesNanos(List<Long> execucoesNanos) {
            this.execucoesNanos = execucoesNanos;
        }

        public long getMediaNanos() {
            return mediaNanos;
        }

        public void setMediaNanos(long mediaNanos) {
            this.mediaNanos = mediaNanos;
        }
    }

    public static class ParallelBenchmarkDTO {
        private int quantidadeThreads;
        private StatisticsResultDTO resultado;
        private BenchmarkTimingDTO tempo;
        private boolean equivalenteAoSequencial;

        public ParallelBenchmarkDTO() {
        }

        public ParallelBenchmarkDTO(int quantidadeThreads,
                                    StatisticsResultDTO resultado,
                                    BenchmarkTimingDTO tempo,
                                    boolean equivalenteAoSequencial) {
            this.quantidadeThreads = quantidadeThreads;
            this.resultado = resultado;
            this.tempo = tempo;
            this.equivalenteAoSequencial = equivalenteAoSequencial;
        }

        public int getQuantidadeThreads() {
            return quantidadeThreads;
        }

        public void setQuantidadeThreads(int quantidadeThreads) {
            this.quantidadeThreads = quantidadeThreads;
        }

        public StatisticsResultDTO getResultado() {
            return resultado;
        }

        public void setResultado(StatisticsResultDTO resultado) {
            this.resultado = resultado;
        }

        public BenchmarkTimingDTO getTempo() {
            return tempo;
        }

        public void setTempo(BenchmarkTimingDTO tempo) {
            this.tempo = tempo;
        }

        public boolean isEquivalenteAoSequencial() {
            return equivalenteAoSequencial;
        }

        public void setEquivalenteAoSequencial(boolean equivalenteAoSequencial) {
            this.equivalenteAoSequencial = equivalenteAoSequencial;
        }
    }
}
