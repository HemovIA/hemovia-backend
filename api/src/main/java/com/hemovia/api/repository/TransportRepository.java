package com.hemovia.api.repository;

import com.hemovia.api.model.Transport;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID> {
    Optional<Transport> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
