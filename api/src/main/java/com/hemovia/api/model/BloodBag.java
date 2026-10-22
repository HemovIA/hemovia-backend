package com.hemovia.api.model;

import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RhFactor;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "blood_bags")
public class BloodBag {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) private RhFactor rhFactor;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodComponent component;
    @NotNull @Column(nullable = false) private LocalDate collectionDate;
    @NotNull @Column(nullable = false) private LocalDate expirationDate;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodBagStatus status;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodType bloodTypeEnum;
    @DecimalMin(value = "0.0", inclusive = false) @Column(name = "volume_ml") private BigDecimal volumeMl;
    @ManyToOne(fetch = FetchType.LAZY) private Person donor;
    @ManyToOne(fetch = FetchType.LAZY) private BloodCenter bloodCenter;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "donation_id") private Donation donation;
    @Version private Long version;

    public BloodBag() { }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RhFactor getRhFactor() { return rhFactor; }
    public void setRhFactor(RhFactor rhFactor) { this.rhFactor = rhFactor; }
    public BloodComponent getComponent() { return component; }
    public void setComponent(BloodComponent component) { this.component = component; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public BloodBagStatus getStatus() { return status; }
    public void setStatus(BloodBagStatus status) { this.status = status; }
    public BloodType getBloodTypeEnum() { return bloodTypeEnum; }
    public void setBloodTypeEnum(BloodType bloodTypeEnum) { this.bloodTypeEnum = bloodTypeEnum; }
    public BigDecimal getVolumeMl() { return volumeMl; }
    public void setVolumeMl(BigDecimal volumeMl) {
        if (volumeMl != null && volumeMl.signum() <= 0) throw new IllegalArgumentException("volumeMl must be positive");
        this.volumeMl = volumeMl;
    }
    public Person getDonor() { return donor; }
    public void setDonor(Person donor) { ensureCompatibleDonor(donor, donation); this.donor = donor; }
    public BloodCenter getBloodCenter() { return bloodCenter; }
    public void setBloodCenter(BloodCenter bloodCenter) { ensureCompatibleCenter(bloodCenter, donation); this.bloodCenter = bloodCenter; }
    public Donation getDonation() { return donation; }
    public void setDonation(Donation donation) {
        if (this.donation != null && donation != null && this.donation != donation)
            throw new IllegalArgumentException("Unlink the blood bag before associating another donation");
        ensureCompatibleDonor(donor, donation);
        ensureCompatibleCenter(bloodCenter, donation);
        this.donation = donation;
    }
    public Long getVersion() { return version; }

    @AssertTrue(message = "expirationDate must not precede collectionDate")
    public boolean isExpirationOnOrAfterCollection() { return collectionDate == null || expirationDate == null || !expirationDate.isBefore(collectionDate); }

    private static void ensureCompatibleDonor(Person bagDonor, Donation donation) {
        if (bagDonor == null || donation == null || donation.getDonor() == null) return;
        UUID bagDonorId = bagDonor.getId(), donationDonorId = donation.getDonor().getId();
        if (bagDonorId != null && donationDonorId != null && !bagDonorId.equals(donationDonorId))
            throw new IllegalArgumentException("blood bag donor must match donation donor when both are known");
    }

    private static void ensureCompatibleCenter(BloodCenter center, Donation donation) {
        if (center == null || donation == null || donation.getBloodCenter() == null) return;
        BloodCenter origin = donation.getBloodCenter();
        if (center != origin && (center.getId() == null || !center.getId().equals(origin.getId())))
            throw new IllegalArgumentException("Blood bag center must match donation center when both are known");
    }
}
