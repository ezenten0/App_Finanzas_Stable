package com.example.risk.web.dto;

import com.example.risk.domain.RiskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RiskCaseRequest(
        @NotBlank(message = "userId es requerido")
        @Size(max = 64)
        String userId,
        @NotNull(message = "score es requerido")
        @Min(value = 0, message = "score mínimo 0")
        @Max(value = 100, message = "score máximo 100")
        Integer score,
        @NotNull(message = "status es requerido")
        RiskStatus status,
        @NotBlank(message = "reason es requerido")
        @Size(max = 200)
        String reason
) {

    public RiskCaseRequest withUser(String userId) {
        return new RiskCaseRequest(userId, score, status, reason);
    }
}
