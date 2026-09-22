package com.hemovia.api.model.DTOs;

import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodType;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

public class BloodBagRequestDTO {
    @NotNull
    private String rhFactor;

    @NotNull
    private String component;

    @NotNull
    private Date collectionDate;

    @NotNull
    private Date expirationDate;

    @NotNull
    private BloodBagStatus status;

    @NotNull
    private BloodType bloodTypeEnum;

    @NotNull
    private UUID donorId;

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

    public Date getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        this.collectionDate = collectionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BloodBagStatus getStatus() {
        return status;
    }

    public void setStatus(BloodBagStatus status) {
        this.status = status;
    }

    public BloodType getBloodTypeEnum() {
        return bloodTypeEnum;
    }

    public void setBloodTypeEnum(BloodType bloodTypeEnum) {
        this.bloodTypeEnum = bloodTypeEnum;
    }

    public UUID getDonorId() {
        return donorId;
    }

    public void setDonorId(UUID donorId) {
        this.donorId = donorId;
    }
}
