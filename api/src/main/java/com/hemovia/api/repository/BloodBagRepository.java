package com.hemovia.api.repository;

import com.hemovia.api.model.BloodBag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BloodBagRepository extends JpaRepository<BloodBag, UUID> {

    Optional<BloodBag> findById(@NotNull UUID id);

    boolean existsById(@NotNull UUID id);

}
