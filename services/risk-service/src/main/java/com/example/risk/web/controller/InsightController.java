package com.example.risk.web.controller;

import com.example.risk.service.InsightsService;
import com.example.risk.web.dto.InsightsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Operation(summary = "Obtiene insights agregados usando cache en Firestore")
    public InsightsResponse getInsights(Authentication authentication) {
        String userId = resolveUserId(authentication, null);
        return InsightsResponse.from(insightsService.getInsights(userId));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Recalcula los insights invalidando la cache",
            description = "Fuerza el recálculo de agregados y actualiza los documentos en Firestore",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de insights enriquecidos",
                            content = @Content(schema = @Schema(implementation = InsightsResponse.class))
                    )
            }
    )
    public InsightsResponse refreshInsights(Authentication authentication) {
        String userId = resolveUserId(authentication, null);
        return InsightsResponse.from(insightsService.recalculate(userId));
    }

    private String resolveUserId(Authentication authentication, String fallback) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return (String) authentication.getPrincipal();
        }
        return fallback == null || fallback.isBlank() ? "anonymous" : fallback;
    }
}
