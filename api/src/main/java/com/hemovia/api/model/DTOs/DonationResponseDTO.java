package com.hemovia.api.model.DTOs;

import java.time.LocalDate;
import java.util.UUID;

public class DonationResponseDTO {
    private UUID donationId;
    private LocalDate collectionDate;
    private String collectionCenter;
    private BloodBagResponseDTO bloodBag;

    public UUID getDonationId() {
        return donationId;
    }

    public void setDonationId(UUID donationId) {
        this.donationId = donationId;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
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
}
