package com.hemovia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "route_nodes")
public class RouteNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    @Column(name = "blood_center_id")
    private UUID bloodCenterId;
    @Column(name = "hospital_id")
    private UUID hospitalId;
    public RouteNode() {
    }

    public RouteNode(String name, double latitude, double longitude, UUID bloodCenterId, UUID hospitalId) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bloodCenterId = bloodCenterId;
        this.hospitalId = hospitalId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public UUID getBloodCenterId() {
        return bloodCenterId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }
}
