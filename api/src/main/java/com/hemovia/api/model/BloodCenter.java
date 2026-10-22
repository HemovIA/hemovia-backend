package com.hemovia.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "blood_centers")
public class BloodCenter {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotBlank @Column(nullable = false) private String name;
    @NotBlank @Column(nullable = false) private String city;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Column(nullable = false) private Double latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Column(nullable = false) private Double longitude;

    public BloodCenter() { }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requiredText(name, "name"); }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = requiredText(city, "city"); }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = coordinate(latitude, -90, 90, "latitude"); }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = coordinate(longitude, -180, 180, "longitude"); }

    private static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
    private static Double coordinate(Double value, double min, double max, String field) {
        if (value == null || !Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException(field + " is outside its valid range");
        return value;
    }
}
