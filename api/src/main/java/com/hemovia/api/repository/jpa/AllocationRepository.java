package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.Allocation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationRepository extends JpaRepository<Allocation, UUID> {
    Optional<Allocation> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
    @Query("select a from Allocation a where a.request.id = :requestId")
    List<Allocation> findByRequestId(@Param("requestId") UUID requestId);
}
