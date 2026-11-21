package com.hemovia.api.model.algorithm.statistics;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DescriptiveStatistics {
    private static final MathContext CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private DescriptiveStatistics() {
    }

    public static Result calculate(List<Frequency> frequencies) {
        Objects.requireNonNull(frequencies, "frequencies");
        List<Frequency> sorted = new ArrayList<>(frequencies);
        sorted.forEach(frequency -> Objects.requireNonNull(frequency, "frequency"));
        sorted.sort(Comparator.comparingLong(Frequency::value));
        long count = 0;
        long total = 0;
        for (Frequency frequency : sorted) {
            count = Math.addExact(count, frequency.count());
            total = Math.addExact(total, Math.multiplyExact(frequency.value(), frequency.count()));
        }
        if (count == 0) {
            return new Result(0, 0, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, true);
        }
        BigDecimal mean = BigDecimal.valueOf(total).divide(BigDecimal.valueOf(count), CONTEXT);
        long leftPosition = (count - 1) / 2;
        long rightPosition = count / 2;
        long cumulative = 0;
        Long left = null;
        Long right = null;
        BigDecimal squaredDifferences = BigDecimal.ZERO;
        for (Frequency frequency : sorted) {
            cumulative = Math.addExact(cumulative, frequency.count());
            if (left == null && cumulative > leftPosition) {
                left = frequency.value();
            }
            if (cumulative > rightPosition && right == null) {
                right = frequency.value();
            }
            BigDecimal difference = BigDecimal.valueOf(frequency.value()).subtract(mean, CONTEXT);
            BigDecimal weightedSquare = difference.multiply(difference, CONTEXT)
                    .multiply(BigDecimal.valueOf(frequency.count()), CONTEXT);
            squaredDifferences = squaredDifferences.add(weightedSquare, CONTEXT);
        }
        BigDecimal median = BigDecimal.valueOf(left).add(BigDecimal.valueOf(right), CONTEXT)
                .divide(BigDecimal.valueOf(2), CONTEXT);
        BigDecimal populationVariance = squaredDifferences.divide(BigDecimal.valueOf(count), CONTEXT);
        BigDecimal sampleVariance = count < 2 ? BigDecimal.ZERO
                : squaredDifferences.divide(BigDecimal.valueOf(count - 1), CONTEXT);
        return new Result(count, total, rounded(mean), rounded(median), rounded(populationVariance),
                rounded(populationVariance.sqrt(CONTEXT)), rounded(sampleVariance),
                rounded(sampleVariance.sqrt(CONTEXT)), count < 2);
    }

    private static BigDecimal rounded(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record Result(long count, long total, BigDecimal mean, BigDecimal median,
                         BigDecimal populationVariance, BigDecimal populationStandardDeviation,
                         BigDecimal sampleVariance, BigDecimal sampleStandardDeviation,
                         boolean insufficientSample) {
    }
}
