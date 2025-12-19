package com.example.ledger.web.dto;

import com.example.ledger.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        String id,
        TransactionType type,
        BigDecimal amount,
        String title,
        String description,
        String category,
        LocalDate date,
        Instant createdAt
) {
}
