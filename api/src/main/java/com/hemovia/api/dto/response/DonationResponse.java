package com.hemovia.api.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(name = "DonationResponse", description = "Doação consultada. bloodBags contém todas as bolsas; bloodBag é nulo quando não há exatamente uma bolsa associada.")
public record DonationResponse(
        @Schema(format = "uuid", example = "d721fd4a-6e38-47d7-8a6d-63c154cfcb8a")
        UUID donationId,

        @Schema(format = "date", example = "2026-10-22")
        String collectionDate,

        @Schema(example = "Hemocentro Central")
        String collectionCenter,

        @Schema(nullable = true, description = "Bolsa singular somente quando a doação possui exatamente uma; nula para zero ou várias.")
        BloodBagResponse bloodBag,

        @ArraySchema(schema = @Schema(implementation = BloodBagResponse.class), arraySchema = @Schema(description = "Todas as bolsas associadas à doação."))
        List<BloodBagResponse> bloodBags,

        @Schema(format = "uuid", nullable = true, description = "Doador vinculado, quando conhecido.")
        UUID donorId,

        @Schema(format = "uuid", nullable = true, description = "Hemocentro vinculado, quando conhecido.")
        UUID bloodCenterId
) {

    public DonationResponse {
        bloodBags = bloodBags == null ? List.of() : List.copyOf(bloodBags);
    }
}
