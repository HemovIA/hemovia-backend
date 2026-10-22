package com.hemovia.api.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_routes")
public class DeliveryRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private BloodCenter origin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Hospital destination;

    /** Route length, in kilometres. */
    @NotNull
    @DecimalMin("0.0")
    @Column(name = "distance_km", nullable = false, precision = 12, scale = 3)
    private BigDecimal distanceKm;

    /** Estimated route duration, in seconds. */
    @NotNull
    @PositiveOrZero
    @Column(name = "estimated_duration_seconds", nullable = false)
    private Long estimatedDurationSeconds;

    @Valid
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "route_waypoints",
            joinColumns = @JoinColumn(name = "route_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(name = "uk_route_waypoints_route_position", columnNames = {"route_id", "position"}))
    @OrderColumn(name = "position", nullable = false)
    private final List<RouteWaypoint> waypoints = new ArrayList<>();

    public DeliveryRoute() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BloodCenter getOrigin() { return origin; }
    public void setOrigin(BloodCenter origin) { this.origin = require(origin, "origin"); }
    public Hospital getDestination() { return destination; }
    public void setDestination(Hospital destination) { this.destination = require(destination, "destination"); }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.signum() < 0) {
            throw new IllegalArgumentException("distanceKm must not be negative");
        }
        this.distanceKm = distanceKm;
    }
    public Long getEstimatedDurationSeconds() { return estimatedDurationSeconds; }
    public void setEstimatedDurationSeconds(Long estimatedDurationSeconds) {
        if (estimatedDurationSeconds == null || estimatedDurationSeconds < 0) {
            throw new IllegalArgumentException("estimatedDurationSeconds must not be negative");
        }
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }
    public List<RouteWaypoint> getWaypoints() { return waypoints; }

    public void addWaypoint(RouteWaypoint waypoint) {
        if (waypoint == null) throw new IllegalArgumentException("waypoint must not be null");
        waypoints.add(waypoint);
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
