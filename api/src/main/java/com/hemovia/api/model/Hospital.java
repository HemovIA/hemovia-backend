package com.hemovia.api.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "hospital")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private int priority;

    public Hospital() {
    }

    public Hospital(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 5){
            System.out.println("Erro - Priority deve estar entre 1 é 5");
        } else {
            this.priority = priority;
        }
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90){

        } else {
            this.latitude = latitude;
        }
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180){

        } else {
            this.longitude = longitude;
        }
    }
}
