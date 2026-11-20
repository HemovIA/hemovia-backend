package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.RouteEdge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteEdgeRepository extends JpaRepository<RouteEdge, UUID> {
    boolean existsByFromNodeIdAndToNodeId(UUID from, UUID to);
}
