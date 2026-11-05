package com.hemovia.api.model.DTOs;

import com.hemovia.api.model.enums.BloodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "DonationRequest", description = "Dados de entrada da doação. É possível informar bloodBagId para vincular uma bolsa existente ou omiti-lo para a criação compatível com o payload legado.")
public class DonationRequestDTO {
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hemocentro Central")
    private String collectionCenter;

    @Schema(format = "uuid", description = "Bolsa existente a vincular; opcional.", example = "42f0c6fb-5c5a-4e46-b2cc-7df77d523a01")
    private UUID bloodBagId;

    @Schema(format = "uuid", description = "Doador vinculado, quando conhecido; opcional.", example = "e75a2dc9-d317-4885-8711-9d4a75bf462b")
    private UUID donorId;

    @Schema(format = "uuid", description = "Hemocentro vinculado, quando conhecido; opcional.", example = "38e2b3af-c29d-47d6-814f-eb30c6f4e531")
    private UUID bloodCenterId;

    @Schema(description = "Tipo sanguíneo da nova bolsa, quando aplicável.", allowableValues = {"A", "B", "AB", "O"}, example = "O")
    private BloodType bloodType;

    @Schema(description = "Fator Rh da nova bolsa, quando aplicável.", example = "POSITIVE")
    private String rhFactor;

    @Schema(description = "Componente da nova bolsa, quando aplicável.", example = "WHOLE_BLOOD")
    private String component;

    @NotNull
    @Schema(format = "date", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22")
    private LocalDate collectionDate;

    @Future(message = "Data de validade deve ser futura")
    @Schema(format = "date", description = "Validade futura da nova bolsa, quando aplicável.", example = "2030-12-31")
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

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }
    public UUID getBloodCenterId() { return bloodCenterId; }
    public void setBloodCenterId(UUID bloodCenterId) { this.bloodCenterId = bloodCenterId; }

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
