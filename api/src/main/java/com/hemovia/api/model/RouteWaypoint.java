package com.hemovia.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Geographic point that belongs to a delivery route; it has no identity of its own. */
@Embeddable
public class RouteWaypoint {
    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(nullable = false)
    private Double longitude;

    public RouteWaypoint() { }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) {
        this.latitude = coordinate(latitude, -90, 90, "latitude");
    }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) {
        this.longitude = coordinate(longitude, -180, 180, "longitude");
    }

    private static Double coordinate(Double value, double min, double max, String field) {
        if (value == null || !Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(field + " is outside its valid range");
        }
        return value;
    }
}
