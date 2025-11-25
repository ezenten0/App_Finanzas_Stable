package com.example.risk.web.dto;

import com.example.risk.domain.RiskStatus;
import java.time.Instant;

public record RiskCaseResponse(
        Long id,
        String userId,
        Integer score,
        RiskStatus status,
        String reason,
        Instant createdAt
) {
}
