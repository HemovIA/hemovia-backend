package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Métricas com duas casas decimais; médias, medianas e desvios em bolsas e variâncias em bolsas².")
public record DemandMetricsResponse(
        @Schema(description = "Quantidade de requisições observadas", minimum = "0", format = "int64") long requestCount,
        @Schema(description = "Total de bolsas requisitadas", minimum = "0", format = "int64") long totalBags,
        @Schema(description = "Média em bolsas, com duas casas decimais") BigDecimal mean,
        @Schema(description = "Mediana em bolsas, com duas casas decimais") BigDecimal median,
        @Schema(description = "Variância populacional em bolsas², com denominador n") BigDecimal populationVariance,
        @Schema(description = "Desvio padrão populacional em bolsas, calculado com denominador n") BigDecimal populationStandardDeviation,
        @Schema(description = "Variância amostral em bolsas², com denominador n - 1; zero para n < 2") BigDecimal sampleVariance,
        @Schema(description = "Desvio padrão amostral em bolsas, calculado com denominador n - 1; zero para n < 2") BigDecimal sampleStandardDeviation,
        @Schema(description = "Indica amostra insuficiente quando n < 2") boolean insufficientSample,
        @Schema(description = "Aviso quando n < 2; null caso contrário", nullable = true) String warning) { }
