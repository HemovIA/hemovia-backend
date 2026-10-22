package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "HospitalRequest", description = "Dados de entrada do hospital. Latitude e longitude são coordenadas geográficas; prioridade vai de 1 a 5.")
public class HospitalRequestDTO {
    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hospital Municipal")
    private String name;

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "São Paulo")
    private String city;

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-90", maximum = "90", description = "Latitude em graus", example = "-23.5505")
    private Double latitude;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-180", maximum = "180", description = "Longitude em graus", example = "-46.6333")
    private Double longitude;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5", description = "Prioridade operacional", example = "3")
    private Integer priority;


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

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
