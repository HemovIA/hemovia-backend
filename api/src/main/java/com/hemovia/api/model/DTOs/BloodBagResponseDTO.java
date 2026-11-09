package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "BloodBagResponse", description = "Bolsa de sangue retornada pela API.")
public class BloodBagResponseDTO {
    @Schema(format = "uuid", example = "42f0c6fb-5c5a-4e46-b2cc-7df77d523a01")
    private UUID bloodBagId;
    @Schema(example = "O")
    private String bloodType;
    @Schema(example = "POSITIVE")
    private String rhFactor;
    @Schema(format = "date", example = "2030-12-31")
    private String expirationDate;
    @Schema(example = "WHOLE_BLOOD")
    private String component;
    @Schema(format = "date", example = "2026-10-22")
    private String collectionDate;
    @Schema(example = "AVAILABLE")
    private String status;
    @Schema(format = "uuid", nullable = true)
    private UUID donorId;
    @Schema(format = "uuid", nullable = true)
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
