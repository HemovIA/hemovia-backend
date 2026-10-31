package com.hemovia.api.model;

import com.hemovia.api.model.enums.TransportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** A transport execution; its bags are derived from the allocation items. */
@Entity
@Table(name = "transports")
public class Transport {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @NotNull @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "allocation_id", nullable = false, unique = true)
    private Allocation allocation;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private DeliveryRoute route;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TransportStatus status = TransportStatus.PLANNED;

    @NotNull @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at") private Instant completedAt;
    @Version private Long version;

    public Transport() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Allocation getAllocation() { return allocation; }
    public void setAllocation(Allocation allocation) { this.allocation = require(allocation, "allocation"); }
    public DeliveryRoute getRoute() { return route; }
    public void setRoute(DeliveryRoute route) { this.route = require(route, "route"); }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = require(vehicle, "vehicle"); }
    public TransportStatus getStatus() { return status; }
    public void setStatus(TransportStatus status) { this.status = require(status, "status"); }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = require(startedAt, "startedAt"); }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) {
        if (completedAt != null && startedAt != null && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("completedAt must not precede startedAt");
        }
        this.completedAt = completedAt;
    }
    public Long getVersion() { return version; }

    @AssertTrue(message = "completedAt must not precede startedAt")
    public boolean isCompletionOnOrAfterStart() {
        return completedAt == null || startedAt == null || !completedAt.isBefore(startedAt);
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
