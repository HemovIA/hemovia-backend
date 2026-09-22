package com.hemovia.api.repository;

import com.hemovia.api.model.Donation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    Optional<Donation> findById(@NotNull UUID id);

    boolean existsById(@NotNull UUID id);
}
