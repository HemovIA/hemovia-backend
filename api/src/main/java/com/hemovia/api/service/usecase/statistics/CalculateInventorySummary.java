package com.hemovia.api.service.usecase.statistics;

import com.hemovia.api.dto.internal.InventoryCount;
import com.hemovia.api.dto.response.BloodTypeInventoryResponse;
import com.hemovia.api.dto.response.ComponentInventoryResponse;
import com.hemovia.api.dto.response.InventoryCombinationResponse;
import com.hemovia.api.dto.response.InventorySummaryResponse;
import com.hemovia.api.model.algorithm.statistics.PercentageDistribution;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RhFactor;
import com.hemovia.api.repository.port.StatisticsStore;
import com.hemovia.api.service.port.output.ClockProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalculateInventorySummary {
    private final StatisticsStore store;
    private final ClockProvider clock;

    public CalculateInventorySummary(StatisticsStore store, ClockProvider clock) {
        this.store = store;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public InventorySummaryResponse execute() {
        Instant generatedAt = clock.now();
        LocalDate referenceDate = generatedAt.atZone(ZoneOffset.UTC).toLocalDate();
        BloodType[] types = BloodType.values();
        RhFactor[] factors = RhFactor.values();
        BloodComponent[] components = BloodComponent.values();
        long[][][] counts = new long[types.length][factors.length][components.length];
        long total = 0;
        for (InventoryCount row : store.inventory(referenceDate)) {
            int t = row.bloodType().ordinal();
            int r = row.rhFactor().ordinal();
            int c = row.component().ordinal();
            counts[t][r][c] = Math.addExact(counts[t][r][c], row.count());
            total = Math.addExact(total, row.count());
        }
        List<Long> typeCounts = new ArrayList<>();
        List<Long> componentCounts = new ArrayList<>();
        List<Long> combinationCounts = new ArrayList<>();
        for (int t = 0; t < types.length; t++) {
            for (int r = 0; r < factors.length; r++) {
                long sum = 0;
                for (int c = 0; c < components.length; c++) {
                    sum = Math.addExact(sum, counts[t][r][c]);
                    combinationCounts.add(counts[t][r][c]);
                }
                typeCounts.add(sum);
            }
        }
        for (int c = 0; c < components.length; c++) {
            long sum = 0;
            for (int t = 0; t < types.length; t++) {
                for (int r = 0; r < factors.length; r++) {
                    sum = Math.addExact(sum, counts[t][r][c]);
                }
            }
            componentCounts.add(sum);
        }
        List<BigDecimal> typePercentages = PercentageDistribution.calculate(typeCounts);
        List<BigDecimal> componentPercentages = PercentageDistribution.calculate(componentCounts);
        List<BigDecimal> combinationPercentages = PercentageDistribution.calculate(combinationCounts);
        List<BloodTypeInventoryResponse> byType = new ArrayList<>();
        List<ComponentInventoryResponse> byComponent = new ArrayList<>();
        List<InventoryCombinationResponse> byCombination = new ArrayList<>();
        int typeIndex = 0;
        int combinationIndex = 0;
        for (BloodType type : types) {
            for (RhFactor factor : factors) {
                byType.add(new BloodTypeInventoryResponse(type, factor, typeCounts.get(typeIndex),
                        typePercentages.get(typeIndex)));
                typeIndex++;
                for (BloodComponent component : components) {
                    byCombination.add(new InventoryCombinationResponse(type, factor, component,
                            combinationCounts.get(combinationIndex), combinationPercentages.get(combinationIndex)));
                    combinationIndex++;
                }
            }
        }
        for (int c = 0; c < components.length; c++) {
            byComponent.add(new ComponentInventoryResponse(components[c], componentCounts.get(c), componentPercentages.get(c)));
        }
        return new InventorySummaryResponse(generatedAt, referenceDate, total, byType, byComponent, byCombination);
    }
}
