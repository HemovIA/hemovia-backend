package com.hemovia.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotNull @Column(nullable = false) private LocalDate collectionDate;
    /** Texto legado mantido para consumidores sem origem estruturada. */
    @NotBlank @Column(nullable = false) private String collectionCenter;
    @ManyToOne(fetch = FetchType.LAZY) private Person donor;
    @ManyToOne(fetch = FetchType.LAZY) private BloodCenter bloodCenter;
    @OneToMany(mappedBy = "donation", fetch = FetchType.LAZY) private final List<BloodBag> bloodBags = new ArrayList<>();
    @Version private Long version;

    public Donation() { }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public String getCollectionCenter() { return collectionCenter; }
    public void setCollectionCenter(String collectionCenter) {
        if (collectionCenter == null || collectionCenter.isBlank()) throw new IllegalArgumentException("collectionCenter must not be blank");
        this.collectionCenter = collectionCenter;
    }
    public Person getDonor() { return donor; }
    public void setDonor(Person donor) {
        for (BloodBag bag : bloodBags) ensureCompatibleDonor(donor, bag.getDonor());
        this.donor = donor;
    }
    public BloodCenter getBloodCenter() { return bloodCenter; }
    public void setBloodCenter(BloodCenter bloodCenter) { this.bloodCenter = bloodCenter; }
    public List<BloodBag> getBloodBags() { return bloodBags; }
    public Long getVersion() { return version; }

    /** @deprecated Use {@link #getBloodBags()}; this is non-null only for exactly one bag. */
    @Deprecated public BloodBag getBloodBag() { return bloodBags.size() == 1 ? bloodBags.getFirst() : null; }

    /** @deprecated Use {@link #addBloodBag(BloodBag)} to model the complete collection. */
    @Deprecated public void setBloodBag(BloodBag bloodBag) {
        for (BloodBag existing : new ArrayList<>(bloodBags)) existing.setDonation(null);
        bloodBags.clear();
        if (bloodBag != null) addBloodBag(bloodBag);
    }

    public void addBloodBag(BloodBag bloodBag) {
        if (bloodBag == null) throw new IllegalArgumentException("bloodBag must not be null");
        bloodBag.setDonation(this);
        if (!bloodBags.contains(bloodBag)) bloodBags.add(bloodBag);
    }

    public void removeBloodBag(BloodBag bloodBag) {
        if (bloodBags.remove(bloodBag) && bloodBag.getDonation() == this) bloodBag.setDonation(null);
    }

    private static void ensureCompatibleDonor(Person donationDonor, Person bagDonor) {
        if (donationDonor == null || bagDonor == null || donationDonor.getId() == null || bagDonor.getId() == null) return;
        if (!donationDonor.getId().equals(bagDonor.getId()))
            throw new IllegalArgumentException("blood bag donor must match donation donor when both are known");
    }
}
