package com.example.risk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record InsightsRequest(
        @Schema(description = "Identificador del usuario que genera la alerta", example = "user-123")
        @NotBlank
        String userId,
        @Schema(description = "Presupuestos con su avance mensual", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Valid
        List<BudgetSnapshotRequest> budgets
) {
    public InsightsRequest {
        budgets = budgets == null ? List.of() : budgets;
        userId = userId == null || userId.isBlank() ? "anonymous" : userId;
    }

    public static InsightsRequest empty() {
        return new InsightsRequest("anonymous", List.of());
    }
}
