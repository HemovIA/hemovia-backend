package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.RouteNode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteNodeRepository extends JpaRepository<RouteNode, UUID> {
    Optional<RouteNode> findByBloodCenterId(UUID id);
    Optional<RouteNode> findByHospitalId(UUID id);
}
