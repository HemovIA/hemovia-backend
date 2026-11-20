package com.hemovia.api.model.algorithm.statistics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PercentageDistribution {
    private static final BigInteger BASIS_POINTS = BigInteger.valueOf(10_000);

    private PercentageDistribution() {
    }

    public static List<BigDecimal> calculate(List<Long> counts) {
        Objects.requireNonNull(counts, "counts");
        BigInteger total = BigInteger.ZERO;
        for (Long count : counts) {
            Objects.requireNonNull(count, "count");
            if (count < 0) {
                throw new IllegalArgumentException("Counts must be non-negative");
            }
            total = total.add(BigInteger.valueOf(count));
        }
        List<BigDecimal> result = new ArrayList<>(counts.size());
        if (total.signum() == 0) {
            for (int i = 0; i < counts.size(); i++) {
                result.add(BigDecimal.ZERO.setScale(2));
            }
            return List.copyOf(result);
        }
        List<Remainder> remainders = new ArrayList<>(counts.size());
        int allocated = 0;
        for (int i = 0; i < counts.size(); i++) {
            BigInteger numerator = BigInteger.valueOf(counts.get(i)).multiply(BASIS_POINTS);
            BigInteger[] division = numerator.divideAndRemainder(total);
            int points = division[0].intValueExact();
            allocated += points;
            result.add(BigDecimal.valueOf(points, 2));
            remainders.add(new Remainder(i, division[1]));
        }
        remainders.sort(Comparator.comparing(Remainder::remainder).reversed()
                .thenComparingInt(Remainder::index));
        for (int i = 0; i < 10_000 - allocated; i++) {
            int index = remainders.get(i).index();
            result.set(index, result.get(index).add(new BigDecimal("0.01")));
        }
        return List.copyOf(result);
    }

    private record Remainder(int index, BigInteger remainder) {
    }
}
