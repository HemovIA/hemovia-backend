package com.hemovia.api.repository;

import com.hemovia.api.model.BloodRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, UUID> {
    Optional<BloodRequest> findById(@NotNull UUID id);
    boolean existsById(@NotNull UUID id);
}
