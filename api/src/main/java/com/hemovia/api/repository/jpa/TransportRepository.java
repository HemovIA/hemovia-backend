package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.Transport;
import com.hemovia.api.model.enums.TransportStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID> {
    Optional<Transport> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
    @Query("select t.allocation.request.id from Transport t where t.id = :id")
    Optional<UUID> findRequestId(@Param("id") UUID id);
    boolean existsByAllocationId(UUID allocationId);
    Optional<Transport> findByAllocationId(UUID allocationId);
    boolean existsByVehicleIdAndStatus(UUID vehicleId, TransportStatus status);
    @Query("""
            select count(t) from Transport t where t.allocation.request.id = :requestId
              and t.status = com.hemovia.api.model.enums.TransportStatus.IN_TRANSIT
            """)
    long countInTransitByRequestId(@Param("requestId") UUID requestId);
}

