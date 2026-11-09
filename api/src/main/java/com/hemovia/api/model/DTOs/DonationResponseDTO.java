package com.hemovia.api.model.DTOs;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DonationResponseDTO {
    private UUID donationId;
    /** ISO-8601 civil date, deliberately serialized without a timezone. */
    private String collectionDate;
    private String collectionCenter;
    private BloodBagResponseDTO bloodBag;
    private List<BloodBagResponseDTO> bloodBags = List.of();
    private UUID donorId;
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
