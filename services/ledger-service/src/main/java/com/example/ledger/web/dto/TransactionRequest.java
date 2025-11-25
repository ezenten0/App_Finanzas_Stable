package com.example.ledger.web.dto;

import com.example.ledger.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotBlank(message = "title es requerido")
        @Size(max = 200)
        String title,
        @NotNull(message = "type es requerido")
        @Schema(description = "Tipo de transacción aceptado por el backend", allowableValues = {"CREDIT", "DEBIT"}, example = "CREDIT")
        TransactionType type,
        @NotNull(message = "amount es requerido")
        @Positive(message = "amount debe ser mayor a cero")
        BigDecimal amount,
        @NotBlank(message = "description es requerida")
        @Size(max = 200)
        String description,
        @NotBlank(message = "category es requerida")
        @Size(max = 120)
        String category,
        @NotBlank(message = "date es requerida")
        @Size(min = 10, max = 10, message = "date debe usar formato ISO-8601 (YYYY-MM-DD)")
        String date
) {
}
