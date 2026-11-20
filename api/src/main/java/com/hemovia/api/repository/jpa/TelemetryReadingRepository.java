package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.TelemetryReading;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, UUID> {
    Optional<TelemetryReading> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
