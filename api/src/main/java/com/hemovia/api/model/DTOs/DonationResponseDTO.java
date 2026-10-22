package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "DonationResponse", description = "Doação consultada. bloodBags contém todas as bolsas; bloodBag é nulo quando não há exatamente uma bolsa associada.")
public class DonationResponseDTO {
    @Schema(format = "uuid", example = "d721fd4a-6e38-47d7-8a6d-63c154cfcb8a")
    private UUID donationId;
    /** ISO-8601 civil date, deliberately serialized without a timezone. */
    @Schema(format = "date", example = "2026-10-22")
    private String collectionDate;
    @Schema(example = "Hemocentro Central")
    private String collectionCenter;
    @Schema(nullable = true, description = "Bolsa singular somente quando a doação possui exatamente uma; nula para zero ou várias.")
    private BloodBagResponseDTO bloodBag;
    @ArraySchema(schema = @Schema(implementation = BloodBagResponseDTO.class), arraySchema = @Schema(description = "Todas as bolsas associadas à doação."))
    private List<BloodBagResponseDTO> bloodBags = List.of();
    @Schema(format = "uuid", nullable = true, description = "Doador vinculado, quando conhecido.")
    private UUID donorId;
    @Schema(format = "uuid", nullable = true, description = "Hemocentro vinculado, quando conhecido.")
    private UUID bloodCenterId;

    public UUID getDonationId() {
        return donationId;
    }

    public void setDonationId(UUID donationId) {
        this.donationId = donationId;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate == null ? null : collectionDate.toString();
    }

    public String getCollectionCenter() {
        return collectionCenter;
    }

    public void setCollectionCenter(String collectionCenter) {
        this.collectionCenter = collectionCenter;
    }

    public BloodBagResponseDTO getBloodBag() {
        return bloodBag;
    }

    public void setBloodBag(BloodBagResponseDTO bloodBag) {
        this.bloodBag = bloodBag;
    }
    public List<BloodBagResponseDTO> getBloodBags() { return bloodBags; }
    public void setBloodBags(List<BloodBagResponseDTO> bloodBags) { this.bloodBags = bloodBags == null ? List.of() : List.copyOf(bloodBags); }
    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }
    public UUID getBloodCenterId() { return bloodCenterId; }
    public void setBloodCenterId(UUID bloodCenterId) { this.bloodCenterId = bloodCenterId; }
}
