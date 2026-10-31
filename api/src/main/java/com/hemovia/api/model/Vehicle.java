package com.hemovia.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotBlank @Column(nullable = false, unique = true) private String code;
    @NotNull @Positive @Column(nullable = false) private Integer capacity;

    public Vehicle() { }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code must not be blank");
        this.code = code;
    }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }
}
