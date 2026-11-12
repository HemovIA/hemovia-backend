package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.enums.BloodComponent;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloodBagRepository extends JpaRepository<BloodBag, UUID> {
    Optional<BloodBag> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select bag from BloodBag bag
            where bag.bloodCenter.id = :centerId
              and bag.status = com.hemovia.api.model.enums.BloodBagStatus.AVAILABLE
              and bag.component = :component
              and bag.expirationDate > :today
            order by bag.id
            """)
    List<BloodBag> lockEligible(@Param("centerId") UUID centerId, @Param("component") BloodComponent component,
                               @Param("today") LocalDate today);

    @Query("""
            select item.bloodBag.id from AllocationItem item
            where item.bloodBag.id in (:ids)
              and item.status in (com.hemovia.api.model.enums.ReservationStatus.RESERVED,
                                  com.hemovia.api.model.enums.ReservationStatus.IN_TRANSIT)
            """)
    List<UUID> findActivelyReservedIds(@Param("ids") List<UUID> ids);
}
