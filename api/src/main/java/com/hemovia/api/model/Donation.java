package com.hemovia.api.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(nullable = false)
    private String collectionCenter;

    @OneToOne(cascade = CascadeType.ALL)
    private BloodBag bloodBag;

    public Donation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public BloodBag getBloodBag() {
        return bloodBag;
    }

    public void setBloodBag(BloodBag bloodBag) {
        this.bloodBag = bloodBag;
    }
}
