package com.hemovia.api.repository.port;

import com.hemovia.api.dto.internal.DemandCount;
import com.hemovia.api.dto.internal.InventoryCount;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface StatisticsStore {
    List<InventoryCount> inventory(LocalDate referenceDate);

    List<DemandCount> demand(Instant from, Instant to);
}
