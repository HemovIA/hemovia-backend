package com.hemovia.api.model;

import com.hemovia.api.model.enums.ReservationStatus;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "allocation_items", uniqueConstraints = @UniqueConstraint(
        name = "uk_allocation_items_allocation_bag", columnNames = {"allocation_id", "blood_bag_id"}))
public class AllocationItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "allocation_id", nullable = false) private Allocation allocation;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blood_bag_id", nullable = false) private BloodBag bloodBag;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.RESERVED;
    @NotNull @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "released_at") private Instant releasedAt;

    public AllocationItem() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Allocation getAllocation() { return allocation; }
    public void setAllocation(Allocation allocation) { this.allocation = require(allocation, "allocation"); }
    public BloodBag getBloodBag() { return bloodBag; }
    public void setBloodBag(BloodBag bloodBag) { this.bloodBag = require(bloodBag, "bloodBag"); }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = require(status, "status"); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = require(createdAt, "createdAt"); }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
