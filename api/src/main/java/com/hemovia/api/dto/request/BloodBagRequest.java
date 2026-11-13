package com.hemovia.api.dto.request;

import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "BloodBagRequest", description = "Dados de entrada da bolsa de sangue. volumeMl é medido em mililitros.")
public record BloodBagRequest(
        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "POSITIVE")
        String rhFactor,

        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "WHOLE_BLOOD")
        String component,

        @NotNull
        @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22T10:30:00Z")
        Date collectionDate,

        @NotNull
        @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2030-12-31T23:59:59Z")
        Date expirationDate,

        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AVAILABLE", "RESERVED", "IN_TRANSIT", "DELIVERED", "DISCARDED"}, example = "AVAILABLE")
        BloodBagStatus status,

        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"A", "B", "AB", "O"}, example = "O")
        BloodType bloodTypeEnum,

        @Schema(format = "uuid", description = "Doador vinculado, quando conhecido; opcional.")
        UUID donorId,

        @Schema(format = "uuid", description = "Hemocentro vinculado, quando conhecido; opcional.")
        UUID bloodCenterId,

        @Schema(description = "Volume da bolsa em mL; opcional.", example = "450.0")
        BigDecimal volumeMl
) {

    public BloodBagRequest {
        collectionDate = copy(collectionDate);
        expirationDate = copy(expirationDate);
    }

    @Override public Date collectionDate() { return copy(collectionDate); }

    @Override public Date expirationDate() { return copy(expirationDate); }

    private static Date copy(Date date) {
        return date == null ? null : new Date(date.getTime());
    }
}
