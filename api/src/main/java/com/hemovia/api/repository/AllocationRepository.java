package com.hemovia.api.repository;

import com.hemovia.api.model.Allocation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AllocationRepository extends JpaRepository<Allocation, UUID> {
    Optional<Allocation> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
