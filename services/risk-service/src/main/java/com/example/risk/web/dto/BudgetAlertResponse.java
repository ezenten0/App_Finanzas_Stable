package com.example.risk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BudgetAlertResponse(
        @Schema(description = "Categoría del presupuesto", example = "Alimentos")
        String category,
        @Schema(description = "Límite mensual")
        Double limit,
        @Schema(description = "Gasto acumulado")
        Double spent,
        @Schema(description = "Porcentaje de consumo (0.0 - n)")
        Double progress,
        @Schema(description = "warning cuando >=75%, critical cuando supera el 100%")
        String status
) {
}
