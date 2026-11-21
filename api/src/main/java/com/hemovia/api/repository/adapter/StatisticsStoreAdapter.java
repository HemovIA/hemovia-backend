package com.hemovia.api.repository.adapter;

import com.hemovia.api.dto.internal.DemandCount;
import com.hemovia.api.dto.internal.InventoryCount;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.ReservationStatus;
import com.hemovia.api.model.enums.RhFactor;
import com.hemovia.api.repository.port.StatisticsStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class StatisticsStoreAdapter implements StatisticsStore {
    private final EntityManager entityManager;

    public StatisticsStoreAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<InventoryCount> inventory(LocalDate referenceDate) {
        return entityManager.createQuery("""
                select b.bloodTypeEnum as bloodType, b.rhFactor as rhFactor,
                       b.component as component, count(b) as bagCount
                from BloodBag b
                where b.status = :available
                  and b.expirationDate > :referenceDate
                  and b.bloodCenter.deletedAt is null
                  and not exists (
                      select item.id from AllocationItem item
                      where item.bloodBag = b and item.status in :activeStatuses
                  )
                group by b.bloodTypeEnum, b.rhFactor, b.component
                """, Tuple.class)
                .setParameter("available", BloodBagStatus.AVAILABLE)
                .setParameter("referenceDate", referenceDate)
                .setParameter("activeStatuses", List.of(ReservationStatus.RESERVED, ReservationStatus.IN_TRANSIT))
                .getResultList().stream()
                .map(row -> new InventoryCount(row.get("bloodType", BloodType.class),
                        row.get("rhFactor", RhFactor.class), row.get("component", BloodComponent.class),
                        row.get("bagCount", Long.class)))
                .toList();
    }

    @Override
    public List<DemandCount> demand(Instant from, Instant to) {
        StringBuilder jpql = new StringBuilder("""
                select r.hospital.id as hospitalId, r.hospital.name as hospitalName,
                       r.quantity as quantity, count(r) as requestCount
                from BloodRequest r
                where r.status <> :cancelled
                """);
        if (from != null) {
            jpql.append(" and r.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and r.createdAt < :to");
        }
        jpql.append(" group by r.hospital.id, r.hospital.name, r.quantity");

        TypedQuery<Tuple> query = entityManager.createQuery(jpql.toString(), Tuple.class)
                .setParameter("cancelled", RequestStatus.CANCELLED);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        return query.getResultList().stream()
                .map(row -> new DemandCount(row.get("hospitalId", UUID.class),
                        row.get("hospitalName", String.class), row.get("quantity", Integer.class),
                        row.get("requestCount", Long.class)))
                .toList();
    }
}
