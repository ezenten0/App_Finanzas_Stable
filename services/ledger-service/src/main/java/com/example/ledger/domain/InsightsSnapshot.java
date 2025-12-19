package com.example.ledger.domain;

public class InsightsSnapshot {

    private final MonthlySummary monthlySummary;
    private final CategoriesSummary categoriesSummary;
    private final RiskInsight riskInsight;
    private final boolean refreshed;

    public InsightsSnapshot(MonthlySummary monthlySummary, CategoriesSummary categoriesSummary, RiskInsight riskInsight, boolean refreshed) {
        this.monthlySummary = monthlySummary;
        this.categoriesSummary = categoriesSummary;
        this.riskInsight = riskInsight;
        this.refreshed = refreshed;
    }

    public MonthlySummary getMonthlySummary() {
        return monthlySummary;
    }

    public CategoriesSummary getCategoriesSummary() {
        return categoriesSummary;
    }

    public RiskInsight getRiskInsight() {
        return riskInsight;
    }

    public boolean isRefreshed() {
        return refreshed;
    }
}
