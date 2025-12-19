package com.example.risk.service;

import com.example.risk.domain.CategoriesSummary;
import com.example.risk.domain.InsightsSnapshot;
import com.example.risk.domain.MonthlySummary;
import com.example.risk.domain.RiskInsight;
import com.example.risk.domain.RiskStatus;
import com.example.risk.domain.Transaction;
import com.example.risk.domain.TransactionType;
import com.example.risk.repository.InsightsRepository;
import com.example.risk.repository.TransactionRepository;
import com.example.risk.web.dto.BudgetAlertResponse;
import com.example.risk.web.dto.BudgetAlertWebhookRequest;
import com.example.risk.web.dto.BudgetSnapshotRequest;
import com.example.risk.web.dto.RiskCaseRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InsightsService {

    private static final Logger log = LoggerFactory.getLogger(InsightsService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final InsightsRepository insightsRepository;
    private final TransactionRepository transactionRepository;
    private final RiskCaseService riskCaseService;
    private final MarketDataClient marketDataClient;
    private final Clock clock;

    public InsightsService(InsightsRepository insightsRepository, TransactionRepository transactionRepository, RiskCaseService riskCaseService, MarketDataClient marketDataClient) {
        this(insightsRepository, transactionRepository, riskCaseService, marketDataClient, Clock.systemUTC());
    }

    InsightsService(InsightsRepository insightsRepository, TransactionRepository transactionRepository, RiskCaseService riskCaseService, MarketDataClient marketDataClient, Clock clock) {
        this.insightsRepository = insightsRepository;
        this.transactionRepository = transactionRepository;
        this.riskCaseService = riskCaseService;
        this.marketDataClient = marketDataClient;
        this.clock = clock;
    }

    public InsightsSnapshot getInsights(String userId) {
        Optional<MonthlySummary> monthlySummary = insightsRepository.findMonthlySummary(userId);
        Optional<CategoriesSummary> categoriesSummary = insightsRepository.findCategoriesSummary(userId);
        Optional<RiskInsight> riskInsight = insightsRepository.findRiskInsight(userId);

        if (monthlySummary.isPresent() && categoriesSummary.isPresent() && riskInsight.isPresent()) {
            Instant latestCacheUpdate = mostRecent(monthlySummary.get().getUpdatedAt(), categoriesSummary.get().getUpdatedAt(), riskInsight.get().getUpdatedAt());
            Instant latestTransactionUpdate = transactionRepository.findLatestUpdate(userId).orElse(Instant.EPOCH);

            if (!isStale(monthlySummary.get().getUpdatedAt(), categoriesSummary.get().getUpdatedAt(), riskInsight.get().getUpdatedAt())
                    && !isDirty(latestTransactionUpdate, latestCacheUpdate)) {
                return new InsightsSnapshot(monthlySummary.get(), categoriesSummary.get(), riskInsight.get(), false);
            }
        }

        log.debug("Insights cache missing or stale for user {}. Recalculating.", userId);
        return recalculate(userId);
    }

    public InsightsSnapshot recalculate(String userId) {
        List<Transaction> transactions = transactionRepository.findAllForUser(userId);
        Instant now = clock.instant();

        BigDecimal totalIncome = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.CREDIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.DEBIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        Map<String, BigDecimal> incomesByCategory = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCategory() == null || transaction.getCategory().isBlank()) {
                continue;
            }
            if (transaction.getType() == TransactionType.CREDIT) {
                incomesByCategory.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add);
            } else if (transaction.getType() == TransactionType.DEBIT) {
                expensesByCategory.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add);
            }
        }

        MonthlySummary monthlySummary = new MonthlySummary(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                now
        );

        CategoriesSummary categoriesSummary = new CategoriesSummary(expensesByCategory, incomesByCategory, now);
        RiskInsight riskInsight = buildRiskInsight(totalIncome, totalExpense, now);

        insightsRepository.saveMonthlySummary(userId, monthlySummary);
        insightsRepository.saveCategoriesSummary(userId, categoriesSummary);
        insightsRepository.saveRiskInsight(userId, riskInsight);

        return new InsightsSnapshot(monthlySummary, categoriesSummary, riskInsight, true);
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

    public Map<String, Double> fetchFxRates() {
        return marketDataClient.fetchLatestRates();
    }

    private boolean isStale(Instant monthlyUpdated, Instant categoriesUpdated, Instant riskUpdated) {
        Instant threshold = clock.instant().minus(CACHE_TTL);
        return monthlyUpdated.isBefore(threshold) || categoriesUpdated.isBefore(threshold) || riskUpdated.isBefore(threshold);
    }

    private boolean isDirty(Instant latestTransactionUpdate, Instant latestCacheUpdate) {
        return latestTransactionUpdate.isAfter(latestCacheUpdate);
    }

    private Instant mostRecent(Instant monthlyUpdated, Instant categoriesUpdated, Instant riskUpdated) {
        return Stream.of(monthlyUpdated, categoriesUpdated, riskUpdated).max(Instant::compareTo).orElse(Instant.EPOCH);
    }

    private RiskInsight buildRiskInsight(BigDecimal totalIncome, BigDecimal totalExpense, Instant updatedAt) {
        if (totalIncome.signum() == 0 && totalExpense.signum() == 0) {
            return new RiskInsight(20, "LOW", "Aún no hay suficientes movimientos para evaluar riesgos", updatedAt);
        }

        BigDecimal utilization = totalIncome.signum() == 0
                ? BigDecimal.valueOf(1)
                : totalExpense.divide(totalIncome.max(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        int score = utilization.multiply(BigDecimal.valueOf(100)).min(BigDecimal.valueOf(100)).intValue();
        String level;
        String message;
        if (score >= 80) {
            level = "HIGH";
            message = "Tu nivel de gasto es alto respecto a tus ingresos";
        } else if (score >= 50) {
            level = "MEDIUM";
            message = "Tus gastos están creciendo, revisa tus categorías principales";
        } else {
            level = "LOW";
            message = "Tus gastos se mantienen bajo control";
        }
        return new RiskInsight(score, level, message, updatedAt);
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
