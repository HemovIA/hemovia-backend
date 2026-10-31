package com.hemovia.api.repository;

import com.hemovia.api.model.AllocationItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AllocationItemRepository extends JpaRepository<AllocationItem, UUID> {
    Optional<AllocationItem> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
