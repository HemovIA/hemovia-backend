package com.hemovia.api.model.DTOs;

import com.hemovia.api.model.enums.BloodType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class DonationRequestDTO {
    @NotNull
    private String collectionCenter;

    private UUID bloodBagId;

    private BloodType bloodType;

    private String rhFactor;

    private String component;

    @NotNull
    private LocalDate collectionDate;

    @Future(message = "Data de validade deve ser futura")
    private LocalDate expirationDate;

    public String getCollectionCenter() {
        return collectionCenter;
    }

    public void setCollectionCenter(String collectionCenter) {
        this.collectionCenter = collectionCenter;
    }

    public UUID getBloodBagId() {
        return bloodBagId;
    }

    public void setBloodBagId(UUID bloodBagId) {
        this.bloodBagId = bloodBagId;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public String getRhFactor() {
        return rhFactor;
    }

    public void setRhFactor(String rhFactor) {
        this.rhFactor = rhFactor;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
