package com.hemovia.api.repository;

import com.hemovia.api.model.StockMovement;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    Optional<StockMovement> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
