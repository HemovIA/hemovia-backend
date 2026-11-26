package com.hemovia.api.model.algorithm.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PercentageDistributionTest {
    @Test
    @DisplayName("@spec:AC-068 Distribui maiores restos com desempate por ordem")
    void distributesRemaindersInInputOrder() {
        List<Long> input = new ArrayList<>(List.of(1L, 1L, 1L));
        List<BigDecimal> result = PercentageDistribution.calculate(input);
        assertEquals(List.of(new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33")), result);
        assertEquals(new BigDecimal("100.00"), result.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(List.of(1L, 1L, 1L), input);
        assertThrows(UnsupportedOperationException.class, () -> result.add(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("@spec:AC-068 Trata zeros e contagens grandes")
    void handlesZerosAndLargeCounts() {
        assertEquals(List.of(new BigDecimal("0.00"), new BigDecimal("0.00")), PercentageDistribution.calculate(List.of(0L, 0L)));
        assertEquals(List.of(), PercentageDistribution.calculate(List.of()));
        assertEquals(List.of(new BigDecimal("50.00"), new BigDecimal("50.00")), PercentageDistribution.calculate(List.of(Long.MAX_VALUE, Long.MAX_VALUE)));
        assertEquals(List.of(new BigDecimal("0.00"), new BigDecimal("100.00")), PercentageDistribution.calculate(List.of(0L, 1L)));
    }

    @Test
    @DisplayName("@spec:AC-068 Rejeita contagens negativas e nulas")
    void rejectsInvalidInputs() {
        assertThrows(NullPointerException.class, () -> PercentageDistribution.calculate(null));
        assertThrows(NullPointerException.class, () -> PercentageDistribution.calculate(Arrays.asList(1L, null)));
        assertThrows(IllegalArgumentException.class, () -> PercentageDistribution.calculate(List.of(-1L)));
    }
}
