package com.hemovia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "route_edges")
public class RouteEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "from_node_id", nullable = false)
    private UUID fromNodeId;
    @Column(name = "to_node_id", nullable = false)
    private UUID toNodeId;
    @Column(name = "distance_km", nullable = false, precision = 12, scale = 3)
    private BigDecimal distanceKm;
    @Column(name = "travel_seconds", nullable = false)
    private long travelSeconds;
    @Column(nullable = false)
    private boolean bidirectional;
    @Column(name = "geometry_json", columnDefinition = "TEXT")
    private String geometryJson;
    private String provider;
    @Column(name = "calculated_at")
    private Instant calculatedAt;
    public RouteEdge() {
    }

    public RouteEdge(UUID from, UUID to, BigDecimal distanceKm, long seconds, String geometryJson, String provider, Instant calculatedAt) {
        this.fromNodeId = from;
        this.toNodeId = to;
        this.distanceKm = distanceKm;
        this.travelSeconds = seconds;
        this.bidirectional = false;
        this.geometryJson = geometryJson;
        this.provider = provider;
        this.calculatedAt = calculatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFromNodeId() {
        return fromNodeId;
    }

    public UUID getToNodeId() {
        return toNodeId;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public long getTravelSeconds() {
        return travelSeconds;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public String getGeometryJson() {
        return geometryJson;
    }

    public String getProvider() {
        return provider;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
