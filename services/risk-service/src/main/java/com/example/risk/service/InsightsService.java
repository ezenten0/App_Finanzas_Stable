package com.example.risk.service;

import com.example.risk.domain.RiskCase;
import com.example.risk.domain.RiskStatus;
import com.example.risk.web.dto.BudgetAlertResponse;
import com.example.risk.web.dto.BudgetAlertWebhookRequest;
import com.example.risk.web.dto.BudgetSnapshotRequest;
import com.example.risk.web.dto.InsightResponse;
import com.example.risk.web.dto.InsightsRequest;
import com.example.risk.web.dto.RiskCaseRequest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InsightsService {

    private final RiskCaseService riskCaseService;
    private final MarketDataClient marketDataClient;
    private final DecimalFormat df = new DecimalFormat("0.00");

    public InsightsService(RiskCaseService riskCaseService, MarketDataClient marketDataClient) {
        this.riskCaseService = riskCaseService;
        this.marketDataClient = marketDataClient;
    }

    public List<InsightResponse> buildInsights(InsightsRequest request) {
        InsightsRequest safeRequest = request == null ? InsightsRequest.empty() : request;
        List<InsightResponse> insights = new ArrayList<>();

        Map<String, Double> rates = marketDataClient.fetchLatestRates();
        rates.forEach((currency, rate) -> insights.add(new InsightResponse(
                "fx-" + currency,
                "Tasa en " + currency,
                "1 USD = " + df.format(rate) + " " + currency,
                "opportunity",
                "medium",
                "Aprovecha la tasa para pagos internacionales",
                null
        )));

        for (BudgetSnapshotRequest budget : safeRequest.budgets()) {
            double progress = resolveProgress(budget);
            if (progress >= 0.75) {
                String status = progress >= 1.0 ? "critical" : "warning";
                String message = progress >= 1.0
                        ? "Has superado el 100% del límite de " + budget.category() + "."
                        : "Has consumido el " + (int) (progress * 100) + "% de tu presupuesto en " + budget.category() + ".";
                BudgetAlertResponse alert = new BudgetAlertResponse(
                        budget.category(),
                        budget.limit(),
                        budget.spent(),
                        progress,
                        status
                );
                insights.add(new InsightResponse(
                        "budget-" + budget.category(),
                        "Alerta de presupuesto: " + budget.category(),
                        message,
                        "budget",
                        status,
                        "Reduce gastos o ajusta el límite",
                        alert
                ));

                riskCaseService.createOrUpdate(new RiskCaseRequest(
                        safeRequest.userId(),
                        Math.min(100, (int) Math.round(progress * 100)),
                        status.equals("critical") ? RiskStatus.OPEN : RiskStatus.REVIEWING,
                        "Presupuesto " + budget.category() + " al " + (int) (progress * 100) + "%"
                ));
            }
        }

        for (RiskCase riskCase : riskCaseService.findAll()) {
            insights.add(new InsightResponse(
                    "risk-" + riskCase.getId(),
                    "Caso de riesgo " + riskCase.getId(),
                    "El score es " + riskCase.getScore() + " por " + riskCase.getReason(),
                    "warning",
                    riskCase.getStatus().name().toLowerCase(),
                    "Revisa las transacciones recientes",
                    null
            ));
        }

        if (insights.isEmpty()) {
            insights.add(new InsightResponse(
                    UUID.randomUUID().toString(),
                    "Sin alertas",
                    "No se detectaron riesgos ni tasas relevantes.",
                    "savings",
                    "low",
                    "Continúa monitoreando tus metas",
                    null
            ));
        }

        return insights;
    }

    public BudgetAlertResponse handleBudgetAlert(BudgetAlertWebhookRequest request) {
        BudgetAlertWebhookRequest safeRequest = request == null
                ? BudgetAlertWebhookRequest.empty()
                : request;
        double progress = resolveProgress(safeRequest.progress(), safeRequest.spent(), safeRequest.limit());
        String status = progress >= 1.0 ? "critical" : "warning";

        riskCaseService.createOrUpdate(new RiskCaseRequest(
                safeRequest.userId(),
                Math.min(100, (int) Math.round(progress * 100)),
                status.equals("critical") ? RiskStatus.OPEN : RiskStatus.REVIEWING,
                "Presupuesto " + safeRequest.category() + " al " + (int) (progress * 100) + "%"
        ));

        return new BudgetAlertResponse(
                safeRequest.category(),
                safeRequest.limit(),
                safeRequest.spent(),
                progress,
                status
        );
    }

    private double resolveProgress(BudgetSnapshotRequest budget) {
        return resolveProgress(budget.progress(), budget.spent(), budget.limit());
    }

    private double resolveProgress(Double progress, Double spent, Double limit) {
        if (progress != null) {
            return progress;
        }
        if (limit == null || limit == 0.0) {
            return 0.0;
        }
        return spent / limit;
    }
}
