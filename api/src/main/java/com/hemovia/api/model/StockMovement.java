package com.hemovia.api.model;

import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.MovementType;
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
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** Immutable-style audit record for a blood bag state transition. */
@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blood_bag_id", nullable = false)
    private BloodBag bloodBag;

    @NotNull @Enumerated(EnumType.STRING) @Column(name = "previous_status", nullable = false)
    private BloodBagStatus previousStatus;

    @NotNull @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false)
    private BloodBagStatus newStatus;

    @NotNull @Enumerated(EnumType.STRING) @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @NotNull @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "reason") private String reason;

    public StockMovement() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BloodBag getBloodBag() { return bloodBag; }
    public void setBloodBag(BloodBag bloodBag) { this.bloodBag = require(bloodBag, "bloodBag"); }
    public BloodBagStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(BloodBagStatus previousStatus) { this.previousStatus = require(previousStatus, "previousStatus"); }
    public BloodBagStatus getNewStatus() { return newStatus; }
    public void setNewStatus(BloodBagStatus newStatus) { this.newStatus = require(newStatus, "newStatus"); }
    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = require(movementType, "movementType"); }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = require(occurredAt, "occurredAt"); }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
