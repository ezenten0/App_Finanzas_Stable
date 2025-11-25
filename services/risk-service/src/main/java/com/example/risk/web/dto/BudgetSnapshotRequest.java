package com.example.risk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BudgetSnapshotRequest(
        @NotBlank
        @Schema(description = "Categoría del presupuesto", example = "Alimentos")
        String category,
        @NotNull
        @Min(0)
        @Schema(description = "Límite mensual configurado", example = "400.0")
        Double limit,
        @NotNull
        @Min(0)
        @Schema(description = "Gasto acumulado del mes", example = "320.0")
        Double spent,
        @Schema(description = "Progreso calculado (spent/limit)", example = "0.8")
        Double progress
) {
}
