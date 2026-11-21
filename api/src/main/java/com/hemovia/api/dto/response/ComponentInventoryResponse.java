package com.hemovia.api.dto.response;

import com.hemovia.api.model.enums.BloodComponent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record ComponentInventoryResponse(BloodComponent component,
        @Schema(description = "Quantidade de bolsas", minimum = "0", format = "int64") long count,
        @Schema(description = "Percentual com duas casas, rateado pelos maiores restos", minimum = "0", maximum = "100") BigDecimal percentage) { }
