package com.hemovia.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "hospital")
public class Hospital {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotBlank @Column(nullable = false) private String name;
    @NotBlank @Column(nullable = false) private String city;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Column(nullable = false) private Double latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Column(nullable = false) private Double longitude;
    @NotNull @Min(1) @Max(5) @Column(nullable = false) private Integer priority;

    public Hospital() { }
    public Hospital(String name, String city) { setName(name); setCity(city); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requiredText(name, "name"); }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = requiredText(city, "city"); }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) {
        if (priority == null || priority < 1 || priority > 5) throw new IllegalArgumentException("priority must be between 1 and 5");
        this.priority = priority;
    }
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
