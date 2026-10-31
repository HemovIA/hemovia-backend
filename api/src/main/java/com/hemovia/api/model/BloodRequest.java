package com.hemovia.api.model;

import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.RhFactor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @jakarta.persistence.JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
    @NotNull @Enumerated(EnumType.STRING) @Column(name = "blood_type", nullable = false)
    private BloodType bloodType;
    @NotNull @Enumerated(EnumType.STRING) @Column(name = "rh_factor", nullable = false)
    private RhFactor rhFactor;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BloodComponent component;
    /** Number of blood bags requested; it is deliberately not a volume in millilitres. */
    @NotNull @Positive @Column(nullable = false)
    private Integer quantity;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    @NotNull @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @NotNull @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Version private Long version;

    public BloodRequest() { }

    @PrePersist void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = RequestStatus.PENDING;
    }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = require(hospital, "hospital"); }
    public BloodType getBloodType() { return bloodType; }
    public void setBloodType(BloodType bloodType) { this.bloodType = require(bloodType, "bloodType"); }
    public RhFactor getRhFactor() { return rhFactor; }
    public void setRhFactor(RhFactor rhFactor) { this.rhFactor = require(rhFactor, "rhFactor"); }
    public BloodComponent getComponent() { return component; }
    public void setComponent(BloodComponent component) { this.component = require(component, "component"); }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        this.quantity = quantity;
    }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = require(status, "status"); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = require(createdAt, "createdAt"); }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = require(updatedAt, "updatedAt"); }
    public Long getVersion() { return version; }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
