package com.hemovia.api.dto.request;

import com.hemovia.api.model.enums.BloodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "DonationRequest", description = "Dados de entrada da doação. É possível informar bloodBagId para vincular uma bolsa existente ou omiti-lo para a criação compatível com o payload legado.")
public record DonationRequest(
        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Hemocentro Central")
        String collectionCenter,

        @Schema(format = "uuid", description = "Bolsa existente a vincular; opcional.", example = "42f0c6fb-5c5a-4e46-b2cc-7df77d523a01")
        UUID bloodBagId,

        @Schema(format = "uuid", description = "Doador vinculado, quando conhecido; opcional.", example = "e75a2dc9-d317-4885-8711-9d4a75bf462b")
        UUID donorId,

        @Schema(format = "uuid", description = "Hemocentro vinculado, quando conhecido; opcional.", example = "38e2b3af-c29d-47d6-814f-eb30c6f4e531")
        UUID bloodCenterId,

        @Schema(description = "Tipo sanguíneo da nova bolsa, quando aplicável.", allowableValues = {"A", "B", "AB", "O"}, example = "O")
        BloodType bloodType,

        @Schema(description = "Fator Rh da nova bolsa, quando aplicável.", example = "POSITIVE")
        String rhFactor,

        @Schema(description = "Componente da nova bolsa, quando aplicável.", example = "WHOLE_BLOOD")
        String component,

        @NotNull
        @Schema(format = "date", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22")
        LocalDate collectionDate,

        @Future(message = "Data de validade deve ser futura")
        @Schema(format = "date", description = "Validade futura da nova bolsa, quando aplicável.", example = "2030-12-31")
        LocalDate expirationDate
) {
}
