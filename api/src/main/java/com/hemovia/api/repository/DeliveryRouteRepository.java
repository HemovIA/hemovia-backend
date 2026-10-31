package com.hemovia.api.repository;

import com.hemovia.api.model.DeliveryRoute;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, UUID> {
    Optional<DeliveryRoute> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
