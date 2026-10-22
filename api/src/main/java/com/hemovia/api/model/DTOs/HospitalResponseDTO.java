package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "HospitalResponse", description = "Hospital cadastrado.")
public class HospitalResponseDTO {
    @Schema(format = "uuid", example = "0c5c4aaf-546c-4b7d-9b4b-41a3e481a903")
    private UUID id;
    @Schema(example = "Hospital Municipal")
    private String name;
    @Schema(example = "São Paulo")
    private String city;
    @Schema(description = "Latitude em graus", example = "-23.5505")
    private double latitude;
    @Schema(description = "Longitude em graus", example = "-46.6333")
    private double longitude;
    @Schema(description = "Prioridade operacional", example = "3")
    private int priority;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
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

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
