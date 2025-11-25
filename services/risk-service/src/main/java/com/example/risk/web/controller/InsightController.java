package com.example.risk.web.controller;

import com.example.risk.service.InsightsService;
import com.example.risk.web.dto.InsightsEnvelope;
import com.example.risk.web.dto.InsightsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insights")
public class InsightController {

    private final InsightsService insightsService;

    public InsightController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    @Operation(summary = "Obtiene insights predeterminados sin enviar contexto")
    public InsightsEnvelope getInsights() {
        return new InsightsEnvelope(insightsService.buildInsights(InsightsRequest.empty()));
    }

    @PostMapping
    @Operation(
            summary = "Genera insights usando el contexto del móvil",
            description = "Permite enviar presupuestos con su progreso para crear alertas cuando superan el 75% y generar risk-cases",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de insights enriquecidos",
                            content = @Content(schema = @Schema(implementation = InsightsEnvelope.class))
                    )
            }
    )
    public InsightsEnvelope postInsights(@RequestBody(required = false) @jakarta.validation.Valid InsightsRequest request) {
        return new InsightsEnvelope(insightsService.buildInsights(request));
    }
}
