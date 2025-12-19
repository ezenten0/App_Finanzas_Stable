package com.example.ledger.repository;

import com.example.ledger.domain.CategoriesSummary;
import com.example.ledger.domain.MonthlySummary;
import com.example.ledger.domain.RiskInsight;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class InsightsRepository {

    private static final Logger log = LoggerFactory.getLogger(InsightsRepository.class);
    private static final String COLLECTION = "insights";
    private static final String MONTHLY_DOCUMENT = "monthlySummary";
    private static final String CATEGORIES_DOCUMENT = "categoriesSummary";
    private static final String RISK_DOCUMENT = "risk";

    private final Firestore firestore;

    public InsightsRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<MonthlySummary> findMonthlySummary(String userId) {
        return readDocument(userId, MONTHLY_DOCUMENT).map(this::toMonthlySummary);
    }

    public Optional<CategoriesSummary> findCategoriesSummary(String userId) {
        return readDocument(userId, CATEGORIES_DOCUMENT).map(this::toCategoriesSummary);
    }

    public Optional<RiskInsight> findRiskInsight(String userId) {
        return readDocument(userId, RISK_DOCUMENT).map(this::toRiskInsight);
    }

    public void saveMonthlySummary(String userId, MonthlySummary summary) {
        saveDocument(userId, MONTHLY_DOCUMENT, Map.of(
                "totalIncome", summary.getTotalIncome().doubleValue(),
                "totalExpense", summary.getTotalExpense().doubleValue(),
                "netBalance", summary.getNetBalance().doubleValue(),
                "updatedAt", Timestamp.from(summary.getUpdatedAt())
        ));
    }

    public void saveCategoriesSummary(String userId, CategoriesSummary summary) {
        saveDocument(userId, CATEGORIES_DOCUMENT, Map.of(
                "expenses", summary.getExpenses(),
                "incomes", summary.getIncomes(),
                "updatedAt", Timestamp.from(summary.getUpdatedAt())
        ));
    }

    public void saveRiskInsight(String userId, RiskInsight riskInsight) {
        saveDocument(userId, RISK_DOCUMENT, Map.of(
                "score", riskInsight.getScore(),
                "level", riskInsight.getLevel(),
                "message", riskInsight.getMessage(),
                "updatedAt", Timestamp.from(riskInsight.getUpdatedAt())
        ));
    }

    private Optional<DocumentSnapshot> readDocument(String userId, String documentId) {
        try {
            DocumentSnapshot snapshot = userInsights(userId).document(documentId).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading insights", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error reading insights from Firestore", e);
        }
    }

    private void saveDocument(String userId, String documentId, Map<String, Object> payload) {
        try {
            DocumentReference reference = userInsights(userId).document(documentId);
            WriteResult result = reference.set(payload).get();
            log.debug("Persisted insight {} for user {} at {}", documentId, userId, result.getUpdateTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving insights", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error saving insights to Firestore", e);
        }
    }

    private MonthlySummary toMonthlySummary(DocumentSnapshot snapshot) {
        double income = snapshot.getDouble("totalIncome") != null ? snapshot.getDouble("totalIncome") : 0.0;
        double expense = snapshot.getDouble("totalExpense") != null ? snapshot.getDouble("totalExpense") : 0.0;
        double net = snapshot.getDouble("netBalance") != null ? snapshot.getDouble("netBalance") : income - expense;
        Instant updatedAt = Optional.ofNullable(snapshot.getTimestamp("updatedAt"))
                .map(Timestamp::toInstant)
                .orElse(Instant.EPOCH);
        return new MonthlySummary(BigDecimal.valueOf(income), BigDecimal.valueOf(expense), BigDecimal.valueOf(net), updatedAt);
    }

    @SuppressWarnings("unchecked")
    private CategoriesSummary toCategoriesSummary(DocumentSnapshot snapshot) {
        Map<String, Double> expenses = snapshot.get("expenses", Map.class);
        Map<String, Double> incomes = snapshot.get("incomes", Map.class);
        Instant updatedAt = Optional.ofNullable(snapshot.getTimestamp("updatedAt"))
                .map(Timestamp::toInstant)
                .orElse(Instant.EPOCH);
        return new CategoriesSummary(
                convertToBigDecimalMap(expenses),
                convertToBigDecimalMap(incomes),
                updatedAt
        );
    }

    private Map<String, BigDecimal> convertToBigDecimalMap(Map<String, Double> values) {
        if (values == null) {
            return Collections.emptyMap();
        }
        return values.entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> BigDecimal.valueOf(entry.getValue())));
    }

    private RiskInsight toRiskInsight(DocumentSnapshot snapshot) {
        long score = Optional.ofNullable(snapshot.getLong("score")).orElse(0L);
        String level = Optional.ofNullable(snapshot.getString("level")).orElse("LOW");
        String message = Optional.ofNullable(snapshot.getString("message")).orElse("");
        Instant updatedAt = Optional.ofNullable(snapshot.getTimestamp("updatedAt"))
                .map(Timestamp::toInstant)
                .orElse(Instant.EPOCH);
        return new RiskInsight((int) score, level, message, updatedAt);
    }

    private com.google.cloud.firestore.CollectionReference userInsights(String userId) {
        return firestore.collection("users").document(userId).collection(COLLECTION);
    }
}
