package com.example.risk.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class CategoriesSummary {

    private final Map<String, BigDecimal> expenses;
    private final Map<String, BigDecimal> incomes;
    private final Instant updatedAt;

    public CategoriesSummary(Map<String, BigDecimal> expenses, Map<String, BigDecimal> incomes, Instant updatedAt) {
        this.expenses = expenses;
        this.incomes = incomes;
        this.updatedAt = updatedAt;
    }

    public Map<String, BigDecimal> getExpenses() {
        return expenses;
    }

    public Map<String, BigDecimal> getIncomes() {
        return incomes;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
