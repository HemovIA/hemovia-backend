package com.hemovia.api.repository;

import com.hemovia.api.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    boolean existsByCode(String code);
}
