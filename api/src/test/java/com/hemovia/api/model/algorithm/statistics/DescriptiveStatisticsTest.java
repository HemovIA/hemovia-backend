package com.hemovia.api.model.algorithm.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DescriptiveStatisticsTest {
    @Test
    @DisplayName("@spec:AC-070 Calcula métricas populacionais e amostrais")
    void calculatesPopulationAndSample() {
        var result = DescriptiveStatistics.calculate(List.of(new Frequency(10, 1), new Frequency(20, 1), new Frequency(30, 1)));
        assertEquals(3, result.count());
        assertEquals(60, result.total());
        assertEquals(new BigDecimal("20.00"), result.mean());
        assertEquals(new BigDecimal("20.00"), result.median());
        assertEquals(new BigDecimal("66.67"), result.populationVariance());
        assertEquals(new BigDecimal("8.16"), result.populationStandardDeviation());
        assertEquals(new BigDecimal("100.00"), result.sampleVariance());
        assertEquals(new BigDecimal("10.00"), result.sampleStandardDeviation());
        assertFalse(result.insufficientSample());
    }

    @Test
    @DisplayName("@spec:AC-070 @spec:AC-071 Calcula métricas com frequências assimétricas")
    void calculatesAsymmetricFrequencies() {
        var result = DescriptiveStatistics.calculate(List.of(new Frequency(10, 2), new Frequency(20, 1), new Frequency(30, 1)));
        assertEquals(4, result.count());
        assertEquals(70, result.total());
        assertEquals(new BigDecimal("17.50"), result.mean());
        assertEquals(new BigDecimal("15.00"), result.median());
        assertEquals(new BigDecimal("68.75"), result.populationVariance());
        assertEquals(new BigDecimal("8.29"), result.populationStandardDeviation());
        assertEquals(new BigDecimal("91.67"), result.sampleVariance());
        assertEquals(new BigDecimal("9.57"), result.sampleStandardDeviation());
        assertFalse(result.insufficientSample());
    }

    @Test
    @DisplayName("@spec:AC-071 Trata vazio e amostra unitária")
    void handlesEmptyAndSingleSample() {
        var empty = DescriptiveStatistics.calculate(List.of());
        assertEquals(0, empty.count());
        assertEquals(0, empty.total());
        assertEquals(new BigDecimal("0.00"), empty.mean());
        assertEquals(new BigDecimal("0.00"), empty.median());
        assertEquals(new BigDecimal("0.00"), empty.populationVariance());
        assertEquals(new BigDecimal("0.00"), empty.sampleStandardDeviation());
        assertTrue(empty.insufficientSample());
        var single = DescriptiveStatistics.calculate(List.of(new Frequency(7, 1)));
        assertEquals(new BigDecimal("7.00"), single.mean());
        assertEquals(new BigDecimal("7.00"), single.median());
        assertEquals(new BigDecimal("0.00"), single.populationStandardDeviation());
        assertEquals(new BigDecimal("0.00"), single.sampleVariance());
        assertTrue(single.insufficientSample());
    }

    @Test
    @DisplayName("@spec:AC-071 Preserva frequências e calcula mediana par sem expandir")
    void weightedMedianAndLargeCounts() {
        List<Frequency> input = new ArrayList<>(List.of(new Frequency(30, 2), new Frequency(10, 2)));
        var result = DescriptiveStatistics.calculate(input);
        assertEquals(new BigDecimal("20.00"), result.median());
        assertEquals(new BigDecimal("20.00"), result.mean());
        assertEquals(List.of(new Frequency(30, 2), new Frequency(10, 2)), input);
        assertEquals(new BigDecimal("0.00"), DescriptiveStatistics.calculate(List.of(new Frequency(0, 2), new Frequency(1, 1))).median());
        assertEquals(new BigDecimal("1.00"), DescriptiveStatistics.calculate(List.of(new Frequency(1, 1_000_000_000L))).median());
    }

    @Test
    @DisplayName("@spec:AC-070 Mantém precisão com valores grandes próximos")
    void avoidsCancellationForLargeValues() {
        var result = DescriptiveStatistics.calculate(List.of(new Frequency(Integer.MAX_VALUE, 1), new Frequency((long) Integer.MAX_VALUE + 1, 1)));
        assertEquals(new BigDecimal("0.25"), result.populationVariance());
    }

    @Test
    @DisplayName("@spec:AC-071 Rejeita entradas inválidas e overflow")
    void rejectsInvalidInputsAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new Frequency(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Frequency(1, 0));
        assertThrows(NullPointerException.class, () -> DescriptiveStatistics.calculate(null));
        assertThrows(NullPointerException.class, () -> DescriptiveStatistics.calculate(Arrays.asList((Frequency) null)));
        assertThrows(ArithmeticException.class, () -> DescriptiveStatistics.calculate(List.of(new Frequency(0, Long.MAX_VALUE), new Frequency(0, 1))));
        assertThrows(ArithmeticException.class, () -> DescriptiveStatistics.calculate(List.of(new Frequency(Long.MAX_VALUE, 2))));
        assertThrows(ArithmeticException.class, () -> DescriptiveStatistics.calculate(List.of(new Frequency(Long.MAX_VALUE, 1), new Frequency(1, 1))));
    }
}
