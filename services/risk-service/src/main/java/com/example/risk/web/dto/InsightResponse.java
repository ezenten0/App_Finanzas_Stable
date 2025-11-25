package com.example.risk.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InsightResponse(
        String id,
        String title,
        String message,
        String category,
        @JsonProperty("risk_level")
        String riskLevel,
        String action,
        @JsonProperty("budget_alert")
        BudgetAlertResponse budgetAlert
) {
}
