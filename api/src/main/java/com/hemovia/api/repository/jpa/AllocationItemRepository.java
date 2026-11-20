package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.AllocationItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationItemRepository extends JpaRepository<AllocationItem, UUID> {
    Optional<AllocationItem> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
    @Query("select i from AllocationItem i where i.allocation.request.id = :requestId")
    List<AllocationItem> findByRequestId(@Param("requestId") UUID requestId);

    @Query("""
            select count(i) from AllocationItem i
            where i.bloodBag.id in :ids and i.allocation.request.id <> :requestId
              and i.status in (com.hemovia.api.model.enums.ReservationStatus.RESERVED,
                               com.hemovia.api.model.enums.ReservationStatus.IN_TRANSIT)
            """)
    long countOtherActiveReservations(@Param("ids") List<UUID> ids, @Param("requestId") UUID requestId);
}
