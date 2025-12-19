package com.example.risk.domain;

import java.math.BigDecimal;
import java.time.Instant;

public class MonthlySummary {

    private final BigDecimal totalIncome;
    private final BigDecimal totalExpense;
    private final BigDecimal netBalance;
    private final Instant updatedAt;

    public MonthlySummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance, Instant updatedAt) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.updatedAt = updatedAt;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
