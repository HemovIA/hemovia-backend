package com.hemovia.api.model.DTOs;

import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "BloodBagRequest", description = "Dados de entrada da bolsa de sangue. volumeMl é medido em mililitros.")
public class BloodBagRequestDTO {
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "POSITIVE")
    private String rhFactor;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "WHOLE_BLOOD")
    private String component;

    @NotNull
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22T10:30:00Z")
    private Date collectionDate;

    @NotNull
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2030-12-31T23:59:59Z")
    private Date expirationDate;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AVAILABLE", "RESERVED", "IN_TRANSIT", "DELIVERED", "DISCARDED"}, example = "AVAILABLE")
    private BloodBagStatus status;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"A", "B", "AB", "O"}, example = "O")
    private BloodType bloodTypeEnum;

    @Schema(format = "uuid", description = "Doador vinculado, quando conhecido; opcional.")
    private UUID donorId;

    @Schema(format = "uuid", description = "Hemocentro vinculado, quando conhecido; opcional.")
    private UUID bloodCenterId;

    @Schema(description = "Volume da bolsa em mL; opcional.", example = "450.0")
    private BigDecimal volumeMl;

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
    public UUID getBloodCenterId() { return bloodCenterId; }
    public void setBloodCenterId(UUID bloodCenterId) { this.bloodCenterId = bloodCenterId; }
    public BigDecimal getVolumeMl() { return volumeMl; }
    public void setVolumeMl(BigDecimal volumeMl) { this.volumeMl = volumeMl; }
}
