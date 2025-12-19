package com.example.risk.web.controller;

import com.example.risk.service.InsightsService;
import com.example.risk.web.dto.BudgetAlertResponse;
import com.example.risk.web.dto.BudgetAlertWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/budget-alerts")
public class BudgetAlertController {

    private final InsightsService insightsService;

    public BudgetAlertController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Recibe alertas de presupuesto disparadas por el móvil",
            description = "Permite registrar umbrales superados para generar risk-cases idempotentes",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Alerta recibida",
                            content = @Content(schema = @Schema(implementation = BudgetAlertResponse.class))
                    )
            }
    )
    public BudgetAlertResponse ingestBudgetAlert(
            @RequestBody(required = false) @Valid BudgetAlertWebhookRequest request,
            Authentication authentication
    ) {
        BudgetAlertWebhookRequest safeRequest = request == null ? BudgetAlertWebhookRequest.empty() : request;
        String userId = authentication != null && authentication.getPrincipal() != null
                ? (String) authentication.getPrincipal()
                : safeRequest.userId();
        BudgetAlertWebhookRequest withUser = new BudgetAlertWebhookRequest(
                userId,
                safeRequest.category(),
                safeRequest.limit(),
                safeRequest.spent(),
                safeRequest.progress(),
                safeRequest.threshold()
        );
        return insightsService.handleBudgetAlert(withUser);
    }
}
