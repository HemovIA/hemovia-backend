package com.hemovia.api.controller;

import com.hemovia.api.dto.response.ApiError;
import com.hemovia.api.dto.response.DemandSummaryResponse;
import com.hemovia.api.dto.response.InventorySummaryResponse;
import com.hemovia.api.service.usecase.statistics.CalculateDemandStatistics;
import com.hemovia.api.service.usecase.statistics.CalculateInventorySummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final CalculateInventorySummary inventory;
    private final CalculateDemandStatistics demand;

    public StatisticsController(CalculateInventorySummary inventory, CalculateDemandStatistics demand) {
        this.inventory = inventory;
        this.demand = demand;
    }

    @Operation(summary = "Distribuição do estoque disponível", security = @SecurityRequirement(name = "cookieAuth"),
            description = "ADMIN e HEMOCENTRO; bolsas AVAILABLE com validade posterior à data atual UTC, em centro ativo e sem reserva RESERVED ou IN_TRANSIT. Contagens em bolsas e percentuais com duas casas, distribuídos por maiores restos para somar 100,00 quando há estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distribuição do estoque", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventorySummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel incompatível", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/inventory")
    public InventorySummaryResponse inventory() {
        return inventory.execute();
    }

    @Operation(summary = "Estatísticas de demanda", security = @SecurityRequirement(name = "cookieAuth"),
            description = "ADMIN e HEMOCENTRO; cada requisição não cancelada é uma observação. Médias, medianas e desvios em bolsas; variâncias em bolsas², com duas casas decimais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas da demanda", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemandSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Papel incompatível", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/demand")
    public DemandSummaryResponse demand(
            @Parameter(description = "Início inclusivo em ISO 8601 com fuso")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Fim exclusivo em ISO 8601 com fuso")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return demand.execute(from, to);
    }
}
