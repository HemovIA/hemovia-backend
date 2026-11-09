package com.hemovia.api.model.DTOs;

import java.util.UUID;

public class BloodBagResponseDTO {
    private UUID bloodBagId;
    private String bloodType;
    private String rhFactor;
    private String expirationDate;
    private String component;
    private String collectionDate;
    private String status;
    private UUID donorId;
    private UUID bloodCenterId;

    public UUID getBloodBagId() {
        return bloodBagId;
    }

    public void setBloodBagId(UUID bloodBagId) {
        this.bloodBagId = bloodBagId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getRhFactor() {
        return rhFactor;
    }

    public void setRhFactor(String rhFactor) {
        this.rhFactor = rhFactor;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getCollectionDate() { return collectionDate; }
    public void setCollectionDate(String collectionDate) { this.collectionDate = collectionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }
    public UUID getBloodCenterId() { return bloodCenterId; }
    public void setBloodCenterId(UUID bloodCenterId) { this.bloodCenterId = bloodCenterId; }
}
