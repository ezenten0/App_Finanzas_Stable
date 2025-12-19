package com.example.risk.web.dto;

import com.example.risk.domain.CategoriesSummary;
import com.example.risk.domain.InsightsSnapshot;
import com.example.risk.domain.MonthlySummary;
import com.example.risk.domain.RiskInsight;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record InsightsResponse(MonthlySummaryDto monthlySummary, CategoriesSummaryDto categoriesSummary, RiskInsightDto risk, boolean refreshed) {

    public static InsightsResponse from(InsightsSnapshot snapshot) {
        return new InsightsResponse(
                MonthlySummaryDto.from(snapshot.getMonthlySummary()),
                CategoriesSummaryDto.from(snapshot.getCategoriesSummary()),
                RiskInsightDto.from(snapshot.getRiskInsight()),
                snapshot.isRefreshed()
        );
    }

    public record MonthlySummaryDto(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance, Instant updatedAt) {
        public static MonthlySummaryDto from(MonthlySummary summary) {
            return new MonthlySummaryDto(summary.getTotalIncome(), summary.getTotalExpense(), summary.getNetBalance(), summary.getUpdatedAt());
        }
    }

    public record CategoriesSummaryDto(Map<String, BigDecimal> expenses, Map<String, BigDecimal> incomes, Instant updatedAt) {
        public static CategoriesSummaryDto from(CategoriesSummary summary) {
            return new CategoriesSummaryDto(summary.getExpenses(), summary.getIncomes(), summary.getUpdatedAt());
        }
    }

    public record RiskInsightDto(int score, String level, String message, Instant updatedAt) {
        public static RiskInsightDto from(RiskInsight insight) {
            return new RiskInsightDto(insight.getScore(), insight.getLevel(), insight.getMessage(), insight.getUpdatedAt());
        }
    }
}
