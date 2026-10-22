package com.hemovia.api.model;

import com.hemovia.api.model.enums.AllocationStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "allocations")
public class Allocation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false) private BloodRequest request;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blood_center_id", nullable = false) private BloodCenter bloodCenter;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AllocationStatus status = AllocationStatus.ALLOCATED;
    @NotNull @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @NotNull @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @OneToMany(mappedBy = "allocation", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<AllocationItem> items = new ArrayList<>();
    @Version private Long version;

    public Allocation() { }

    @PrePersist void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = AllocationStatus.ALLOCATED;
    }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BloodRequest getRequest() { return request; }
    public void setRequest(BloodRequest request) { this.request = require(request, "request"); }
    public BloodCenter getBloodCenter() { return bloodCenter; }
    public void setBloodCenter(BloodCenter bloodCenter) { this.bloodCenter = require(bloodCenter, "bloodCenter"); }
    public AllocationStatus getStatus() { return status; }
    public void setStatus(AllocationStatus status) { this.status = require(status, "status"); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = require(createdAt, "createdAt"); }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = require(updatedAt, "updatedAt"); }
    public List<AllocationItem> getItems() { return items; }
    public Long getVersion() { return version; }

    public void addItem(AllocationItem item) {
        if (item == null) throw new IllegalArgumentException("item must not be null");
        item.setAllocation(this);
        if (!items.contains(item)) items.add(item);
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }
}
