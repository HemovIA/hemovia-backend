package com.hemovia.api.model.DTOs;

import java.util.UUID;

public class BloodBagResponseDTO {
    private UUID bloodBagId;
    private String bloodType;
    private String rhFactor;
    private String expirationDate;

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
}
