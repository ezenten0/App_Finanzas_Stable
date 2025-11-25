package com.example.risk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BudgetAlertWebhookRequest(
        @Schema(description = "Identificador del usuario que disparó la alerta", example = "mobile-user")
        String userId,
        @NotBlank
        @Schema(description = "Categoría afectada", example = "Alimentos")
        String category,
        @NotNull
        @Min(0)
        @Schema(description = "Límite del presupuesto", example = "400.0")
        Double limit,
        @NotNull
        @Min(0)
        @Schema(description = "Gasto acumulado", example = "320.0")
        Double spent,
        @Schema(description = "Progreso calculado", example = "0.8")
        Double progress,
        @Schema(description = "Umbral cruzado", example = "0.75")
        Double threshold
) {
    public static BudgetAlertWebhookRequest empty() {
        return new BudgetAlertWebhookRequest("mobile-user", "", 0.0, 0.0, 0.0, 0.0);
    }
}
